/*
 * Copyright 2017 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.webServices.ambapo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.bind.JAXBException;
import org.cirdles.ambapo.Coordinate;
import org.cirdles.ambapo.UTM;
import static org.cirdles.calamari.constants.CalamariConstants.DEFAULT_PRAWNFILE_NAME;
import static org.cirdles.calamari.utilities.FileUtilities.recursiveDelete;
import org.xml.sax.SAXException;

/**
 *
 * @author evc1996
 */
public class AmbapoFileHandlerService {
    
    public Path generateBulkOutput(
            String myFileName,
            InputStream ambapoFile) throws IOException, JAXBException, SAXException {

        String fileName = myFileName;
        if (myFileName == null) {
            fileName = "ambapofile.csv";
        }

        Path uploadDirectory = Files.createTempDirectory("upload");
        Path ambapoFilePath = uploadDirectory.resolve("ambapo-file.csv");
        Files.copy(ambapoFile, ambapoFilePath);
               
        return ambapoFilePath;
    }
    
    public File generateSoloOutputUTM(UTM utm) throws IOException {
        
        File tempOutputFile = File.createTempFile("output", ".txt");
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempOutputFile));
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("Easting: ");
        sb.append(utm.getEasting());
        
        sb.append("\nNorthing: ");
        sb.append(utm.getNorthing());
        
        sb.append("\nZone Number: ");
        sb.append(utm.getZoneNumber());
        
        sb.append("\nZone Letter: ");
        sb.append(utm.getZoneLetter());
        
        sb.append("\nHemisphere: ");
        sb.append(utm.getHemisphere());
        
        bw.write(sb.toString());
        
        bw.close();
        
        return tempOutputFile;
        
    }
    
    public File generateSoloOutputLatLong(Coordinate coord) throws IOException {
        File tempOutputFile = File.createTempFile("output", ".txt");
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempOutputFile));
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("Latitude: ");
        sb.append(coord.getLatitude());
        
        sb.append("\nLongitude: ");
        sb.append(coord.getLongitude());
        
        sb.append("\nDatum: ");
        sb.append(coord.getDatum().getDatum());
        
        bw.write(sb.toString());
        
        bw.close();
        
        return tempOutputFile;
    }
    
}
