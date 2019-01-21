package no.bibsys.aws.tools;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;

public class StringUtilsTest extends AmazonNamingRestrictions {


    @Test
    public void normalizedStringsShoudComplyWithAmazonConstraints() {
        String branchName = "AUTREG-131_ensure_javascript_linting_is_in_place";
        String normalized = stringUtils.normalizeString(branchName);
        Matcher matcher = amazonPattern.matcher(normalized);
        assertThat(matcher.matches(), is(equalTo(true)));
    }

    @Test
    public void randomString_smallMaxLength_randomStringOfLengthMaxLength() {
        int smallLength = 5;
        String randomString1 = stringUtils.randomString(smallLength);
        String randomString2 = stringUtils.randomString(smallLength);
        assertThat(randomString1.length(), is(equalTo(smallLength)));
        assertThat(randomString2.length(), is(equalTo(smallLength)));
        assertThat(randomString1, is(not(equalTo(randomString2))));
    }

    @Test
    public void randomString_largeMaxLength_randomStringOfLengthNotGreaterThanMaxLength() {
        int largeLegnth = 3000;
        String randomString1 = stringUtils.randomString(largeLegnth);
        String randomString2 = stringUtils.randomString(largeLegnth);
        assertThat(randomString1.length(), is(lessThan(largeLegnth)));
        assertThat(randomString2.length(), is(lessThan(largeLegnth)));
        assertThat(randomString1, is(not(equalTo(randomString2))));
    }


    @Test
    public void randomString_zeroLength_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> stringUtils.randomString(0));
    }

}
