package no.bibsys.aws.tools;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class IoUtilsTest {

    private Path path = Paths.get("github", "pullrequest.json");

    // IOUtilsShould ...
    @Test
    public void inputStreamFromResources_resourcePath_nonEmptyInputStream() throws IOException {

        InputStream inputSteam = IoUtils.inputStreamFromResources(path);
        int x = inputSteam.read();
        assertThat(x, is(not(equalTo(0))));
    }

    @Test
    public void linesfromResource_resourcePath_nonEmptyListOfStrings() throws IOException {
        List<String> list = IoUtils.linesfromResource(path);
        assertThat(list.size(), is(not(equalTo(0))));
    }

    @Test
    public void linesfromResource_resourcePath_nonEmptyString() throws IOException {
        String content = IoUtils.resourceAsString(path);
        assertThat(content.length(), is(not(equalTo(0))));
    }

    @Test
    public void linesfromResource_removeMultipleWhiteSpaces_StringwithoutMultipleWhitespaces() throws IOException {
        String content = IoUtils.resourceAsString(path);
        String trimmed = IoUtils.removeMultipleWhiteSpaces(content);

        assertThat(trimmed.length(), is(not(equalTo(0))));
        assertThat(trimmed, not(matchesPattern("\\s\\s")));
    }
    
    @Test
    public void stringFromFile_file_string() throws URISyntaxException, IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path.toString());
        Path path = Paths.get(resource.toURI());
        String result = IoUtils.fileAsString(path);
        assertThat(result, is(not(equalTo(null))));
        
    }
}
