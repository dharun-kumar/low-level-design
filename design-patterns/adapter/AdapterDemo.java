interface API {
    String getResponse();
}

class REST implements API {
    @Override
    public String getResponse() {
        return "{\n  \"status\": 200,\n  \"response\": {\n    \"name\": \"Dharun\",\n    \"status\": \"Available\"\n  }\n}";
    }
}

class SOAP {
    public String getXMLResponse() {
        return "<soap:Envelope xmlns:soap=\"http://xmlsoap.org\">\n  <soap:Body>\n    <Response>\n      <status>200</status>\n      <data>\n        <name>Kumar</name>\n        <status>Away</status>\n      </data>\n    </Response>\n  </soap:Body>\n</soap:Envelope>";
    }
}

class SOAPAdapter implements API {
    private SOAP legacyRequest = new SOAP();

    @Override
    public String getResponse() {
        String response = legacyRequest.getXMLResponse();
        return transformResponse(response);
    }

    private String transformResponse(String xmlResponse) {
        //TODO transformation logic
        return "{\n  \"status\": 200,\n  \"response\": {\n    \"name\": \"Kumar\",\n    \"status\": \"Away\"\n  }\n}";
    }
}

public class AdapterDemo {
    public static void main(String[] args) {
        REST rest = new REST();
        System.out.println(rest.getResponse());

        SOAP soap = new SOAP();
        System.out.println(soap.getXMLResponse());

        SOAPAdapter soapAdapter = new SOAPAdapter();
        System.out.println(soapAdapter.getResponse());
    }
}