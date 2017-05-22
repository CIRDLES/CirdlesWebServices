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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FilenameUtils;
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
      
    public static String extract(File file, File destination) throws IOException {
        File outFile = null;
        ZipInputStream in = null;
        OutputStream out = null;
        try {
        // Open the ZIP file
            in = new ZipInputStream(new FileInputStream(file));

            // Get the first entry
            ZipEntry entry = null;

            while ((entry = in.getNextEntry()) != null) {
                String outFilename = entry.getName();
                
                // Open the output file
                outFile = new File(destination, outFilename);
                
                if (entry.isDirectory()) {
                outFile.mkdirs();
                } else {
                out = new FileOutputStream(outFile);

                // Transfer bytes from the ZIP file to the output file
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                // Close the stream
                out.close();
                }
            }
        } 
        
        finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return outFile.getPath();
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

        Path reportsFolder = Paths.get(reportsEngine.getFolderToWriteCalamariReportsPath()).getParent().toAbsolutePath();

        Path reports = Files.list(reportsFolder)
                .findFirst().orElseThrow(() -> new IllegalStateException());
        
        Path reportsZip = zip(reports);
        recursiveDelete(reports);

        return reportsZip;
    }
    
    public Path generateReportsZip(
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
        Path prawnFilePathZip = uploadDirectory.resolve("prawn-file.zip");
        
        Files.copy(prawnFile, prawnFilePathZip);
        
        //file path string to extracted xml
        String extract = extract(prawnFilePathZip.toFile(), uploadDirectory.toFile());
        
        Path calamarirReportsFolderAlias = Files.createTempDirectory("reports-destination");
        File reportsDestinationFile = calamarirReportsFolderAlias.toFile();

        reportsEngine.setFolderToWriteCalamariReports(reportsDestinationFile);
        fileName = FilenameUtils.removeExtension(fileName);
        
        // this gives reportengine the name of the Prawnfile for use in report names
        prawnFileHandler.initReportsEngineWithCurrentPrawnFileName(fileName);
        prawnFileHandler.writeReportsFromPrawnFile(extract,
                useSBM,
                userLinFits,
                firstLetterRM);

        Files.delete(prawnFilePathZip);

        Path reportsFolder = Paths.get(reportsEngine.getFolderToWriteCalamariReportsPath()).getParent().toAbsolutePath();

        Path reports = Files.list(reportsFolder)
                .findFirst().orElseThrow(() -> new IllegalStateException());
        
        Path reportsZip = zip(reports);
        recursiveDelete(reports);

        return reportsZip;
    }
}
