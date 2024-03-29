/*
 * Copyright 2018 CIRDLES.
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
package org.cirdles.webServices.requestUtils;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;

/**
 *
 * @author ty
 */
public class JSONUtils {
    public static JSONObject extractRequestJSON(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual);
        return new JSONObject(body);
    }
    
    public static JSONObject createResponseErrorJSON(String message) {
        JSONObject json = new JSONObject();
        json.put("error", true);
        json.put("message", message);
        return json;
    }
}
