package p4analyser.broker.tools;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Iterator;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.HashMap;

public class JsonHandler {
    
    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private JSONArray testInfo;

    public JsonHandler (String fileName) {
        JSONParser parser = new JSONParser();
        
        try {
            Object obj = parser.parse(new FileReader(loader.getResource(fileName).getPath()));
            JSONObject jsonObj = (JSONObject) obj;
            testInfo = (JSONArray) jsonObj.get("all-tests");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, ArrayList<String>> getFileTestInfo() {
        HashMap<String, ArrayList<String> > testFileInfo = new HashMap<String, ArrayList<String>>();

        Iterator<JSONObject> oneTestType = testInfo.iterator();
        while (oneTestType.hasNext()) {
            JSONObject oneType = oneTestType.next();
            String testType = oneType.get("test").toString();
            JSONArray testFiles = (JSONArray) oneType.get("files");
            
            Iterator<JSONObject> oneTestFile = testFiles.iterator();
            while (oneTestFile.hasNext()) {
                String testFileName = oneTestFile.next().get("file").toString();

                if (testFileInfo.containsKey(testFileName)) {
                    ArrayList<String> tmp = testFileInfo.get(testFileName);
                    tmp.add(testType);
                    testFileInfo.replace(testFileName, tmp);
                } else {
                    ArrayList<String> tmp = new ArrayList<String>();
                    tmp.add(testType); 
                    testFileInfo.put(testFileName, tmp);
                }
            }
        }
        return testFileInfo;
    }

    private JSONArray getTestTypeFilesInfo (String testType) {
        Iterator<JSONObject> type = testInfo.iterator();
        while(type.hasNext()) {
            JSONObject t = type.next();
            if (t.get("test").toString().equals(testType)) {
                return ((JSONArray) t.get("files"));
            }
        }
        return new JSONArray();
    }

    private JSONArray getTestTypeOneFileInfo (String testType, String fileName) {
        Iterator<JSONObject> file = getTestTypeFilesInfo(testType).iterator();
        while (file.hasNext()) {
            JSONObject f = file.next();
            if (f.get("file").toString().equals(fileName)) {
                return ((JSONArray) f.get("test-cases"));
            }
        }
        return new JSONArray();
    }


    public ArrayList<Object> getFileTestValues(String fileName, String testType) {
        ArrayList<Object> values = new ArrayList<Object>();
        JSONArray testCases = getTestTypeOneFileInfo(testType, fileName);
        for (Object value : testCases) {
            values.add(((JSONObject) value).get("value"));
        }
        return values;
    }
}
