/*
 * Copyright 2016 CIRDLES
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.webServices.calamari;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import static org.cirdles.calamari.constants.CalamariConstants.DEFAULT_PRAWNFILE_NAME;
import org.cirdles.calamari.core.CalamariReportsEngine;
import org.cirdles.calamari.core.PrawnFileHandler;
import static org.cirdles.calamari.utilities.FileUtilities.recursiveDelete;
import org.xml.sax.SAXException;

/**
 * Created by johnzeringue on 7/27/16.
 */
public class PrawnFileHandlerService {

    private static final Map<String, String> ZIP_FILE_ENV;

    static {
        Map<String, String> zipFileEnv = new HashMap<>();
        zipFileEnv.put("create", "true");

        ZIP_FILE_ENV = Collections.unmodifiableMap(zipFileEnv);
    }

    private final PrawnFileHandler prawnFileHandler;
    private final CalamariReportsEngine reportsEngine;

    public PrawnFileHandlerService() {
        prawnFileHandler = new PrawnFileHandler();
        reportsEngine = prawnFileHandler.getReportsEngine();
    }

    private Path zip(Path target) throws IOException {
        Path zipFilePath = target.resolveSibling("reports.zip");
        
        try (FileSystem zipFileFileSystem = FileSystems.newFileSystem(
                URI.create("jar:" + zipFilePath.toUri()), ZIP_FILE_ENV)) {

            Files.list(target).forEach(entry -> {
                try {
                    Files.copy(entry, zipFileFileSystem.getPath("/" + entry.getFileName()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        return zipFilePath;
    }

    public Path generateReports(
            String myFileName,
            InputStream prawnFile,
            boolean useSBM,
            boolean userLinFits,
            String firstLetterRM) throws IOException, JAXBException, SAXException {

        String fileName = myFileName;
        if (myFileName == null) {
            fileName = DEFAULT_PRAWNFILE_NAME;
        }

        Path uploadDirectory = Files.createTempDirectory("upload");
        Path prawnFilePath = uploadDirectory.resolve("prawn-file.xml");
        Files.copy(prawnFile, prawnFilePath);

        Path calamarirReportsFolderAlias = Files.createTempDirectory("reports-destination");
        File reportsDestinationFile = calamarirReportsFolderAlias.toFile();

        reportsEngine.setFolderToWriteCalamariReports(reportsDestinationFile);

        // this gives reportengine the name of the Prawnfile for use in report names
        prawnFileHandler.initReportsEngineWithCurrentPrawnFileName(fileName);

        prawnFileHandler.writeReportsFromPrawnFile(prawnFilePath.toString(),
                useSBM,
                userLinFits,
                firstLetterRM);

        Files.delete(prawnFilePath);

        Path reportsFolder = Paths.get(reportsEngine.getFolderToWriteCalamariReportsPath()).getParent();

        Path reports = Files.list(reportsFolder)
                .findFirst().orElseThrow(() -> new IllegalStateException());
        
        Path reportsZip = zip(reports);
        recursiveDelete(reports);

        return reportsZip;
    }

}
