package no.bibsys.aws.swaggerhub;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SwaggerHubUrlFormatter {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerHubUrlFormatter.class);
    private static final String SWAGGERHUB_RESTAPI_URL = "https://api.swaggerhub.com/apis";
    private final transient URI requestURL;


    public SwaggerHubUrlFormatter(SwaggerHubInfo swaggerHubInfo, String apiVersion,
            Map<String, String> requestParameters) throws URISyntaxException {

        URI  apiUri=apiUri(swaggerHubInfo,apiVersion);
        this.requestURL = urlFormatter(apiUri, requestParameters);

    }


    public URI getRequestURL() {
        return requestURL;
    }


    private URI apiUri(SwaggerHubInfo info, String apiVersion) throws URISyntaxException {
        if (apiVersion!=null) {

            URI uri = new URI(String.format("%s/%s/%s/%s", SWAGGERHUB_RESTAPI_URL, info.getSwaggerOrganization(),
                    info.getApiId(), apiVersion));
            if (logger.isErrorEnabled()) {
                logger.error("SwaggerHub URL:{}", uri);
            }
            return uri;
        } else {
            URI uri = new URI(
                    String.format("%s/%s/%s", SWAGGERHUB_RESTAPI_URL, info.getSwaggerOrganization(), info.getApiId()));
            if (logger.isErrorEnabled()) {
                logger.error("SwaggerHub URL:{}", uri);
            }
            return uri;
        }

    }


    private URI urlFormatter(URI apiAddress, Map<String, String> requestParameters) {

        Optional<String> parameterOpt = joinParametersToString(requestParameters);

        // remove the last slash if there is any
        String host = apiAddress.toString().replaceAll("/$", "");
        if (parameterOpt.isPresent()) {
            String parametersString = parameterOpt.get();
            String uriString = String.join("?", host, parametersString);
            URI uri = URI.create(uriString);
            return uri;
        } else {
            return URI.create(host);
        }

    }


    private Optional<String> joinParametersToString(Map<String, String> parameters) {
        return parameters.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .reduce((str1, str2) -> String.join("&", str1, str2));
    }


}
