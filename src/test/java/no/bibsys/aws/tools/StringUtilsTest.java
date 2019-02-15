package no.bibsys.aws.tools;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringUtilsTest extends AmazonNamingRestrictions {
    
    private static final int SMALL_LENGTH = 2;
    private static final int BIG_LENGTH = 10;
    private static final StringUtils stringUtils = new StringUtils();
    private static final String BRANCH_GIVING_INVALID_STACK_IN_AWS = "AUTREG-131_ensure_javascript_linting_is_in_place";
    private static final String RANDOM_STRING_OF_MEDIUM_LENGHT = "ABDCEFG";
    
    @Test
    public void normalizedStringsShoudComplyWithAmazonConstraints() {
        String branchName = BRANCH_GIVING_INVALID_STACK_IN_AWS;
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
    
    @Test
    public void shortString_stringNonZeroLength_stringWithGivenLength() {
        String test = RANDOM_STRING_OF_MEDIUM_LENGHT;
        String short1 = stringUtils.shortNormalizedString(test, SMALL_LENGTH);
        assertThat(short1.length(), is(equalTo(2)));
        
        String short2 = stringUtils.shortNormalizedString(test, BIG_LENGTH);
        assertThat(short2.length(), is(equalTo(test.length())));
    }
}
