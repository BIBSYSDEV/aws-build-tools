package no.bibsys.aws.tools;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import java.util.regex.Matcher;
import org.junit.Test;

public class StringUtilsTest extends AmazonNamingRestrictions {


    @Test
    public void normalizedStringsShoudComplyWithAmazonConstraints() {
        String branchName = "AUTREG-131_ensure_javascript_linting_is_in_place";
        String normalized = stringUtils.normalizeString(branchName);
        Matcher matcher = amazonPattern.matcher(normalized);
        assertThat(matcher.matches(), is(equalTo(true)));

    }

}
