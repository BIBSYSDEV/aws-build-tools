package no.bibsys.aws.lambda.handlers;

import no.bibsys.aws.lambda.handlers.templates.ApiMessageParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ApiMessageParserTest extends LocalTest {

    private static final String HEADER = "header1";
    private static final String ANOTHER_HEADER = "header2";
    private static final String HEADER_VALUE = "Header1Value";
    private static final String ANOTHER_HEADER_VALUE = "Header2Value";
    private static final int NUMBER_OF_HEADERS = 2;
    private static final String BODY_STRING = "Some body data goes here";


    @Test
    public void getHeadersFromJson_headersFieldObject_headersMap() throws IOException {

        ApiMessageParser<InputClass> apiMessageParser = new ApiMessageParser<>();
        Map<String, String> headers =
                apiMessageParser.getHeadersFromJson(apiGatewayMessageWithObjectAsBody(InputClass.create()));
        assertThat(headers.size(), is(equalTo(NUMBER_OF_HEADERS)));
        assertThat(headers.get(HEADER), is(equalTo(HEADER_VALUE)));
        assertThat(headers.get(ANOTHER_HEADER), is(equalTo(ANOTHER_HEADER_VALUE)));
    }

    @Test
    public void getBodyElementFromJson_Stringbody_string() throws IOException {
        ApiMessageParser<String> apiMessageParser = new ApiMessageParser<>();
        String body = apiMessageParser.getBodyElementFromJson(apiGatewayMessageWithStringAsBody(), String.class);
        assertThat(body, is(equalTo(BODY_STRING)));
    }

    @Test
    public void getBodyElementFromJson_Objectbody_object() throws IOException {
        ApiMessageParser<InputClass> apiMessageParser = new ApiMessageParser<>();
        InputClass body = apiMessageParser
                .getBodyElementFromJson(apiGatewayMessageWithObjectAsBody(InputClass.create()), InputClass.class);
        InputClass expected = InputClass.create();
        assertThat(body.getId(), is(equalTo(expected.getId())));
        assertThat(body.getRandomField(), is(equalTo(expected.getRandomField())));
    }

    @Test
    public void getBodyElemtFromJson_StringRepresenationOfObject_object() throws IOException {
        ApiMessageParser<InputClass> apiMessageParser = new ApiMessageParser<>();
        InputClass body =
                apiMessageParser.getBodyElementFromJson(apiGatewayMessageWithSerializedJsonBody(), InputClass.class);
        InputClass expected = InputClass.create();
        assertThat(body.getId(), is(equalTo(expected.getId())));
        assertThat(body.getRandomField(), is(equalTo(expected.getRandomField())));

    }

    @Test
    public void getBodyElemtFromJson_nullObject_null() throws IOException {
        ApiMessageParser<InputClass> apiMessageParser = new ApiMessageParser<>();
        InputClass body = apiMessageParser.getBodyElementFromJson(apiGatewayMessageWitNullBody(), InputClass.class);

        assertThat(body, is(equalTo(null)));

    }

}
