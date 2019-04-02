package no.bibsys.aws.route53;

import no.bibsys.aws.cloudformation.Stage;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StaticUrlInfoTest {
    
    public static final String RECORDSET_NAME_LAST_CHARACTER = ".";
    public static final String INVALID_ZONE_NAME = "zone.name";
    private static final String VALID_ZONE_NAME = "zone.name.";
    private static final String VALID_APPLICATION_URL = "application.url.";
    private static final String INVALID_APPLICATION_URL = "application.url";
    private final StaticUrlInfo urlInfo;
    
    public StaticUrlInfoTest() {
        urlInfo = new StaticUrlInfo(VALID_ZONE_NAME, VALID_APPLICATION_URL, Stage.TEST);
    }
    
    @Test
    public void constructorShouldRejectInvalidEntries() {
        assertThrows(IllegalArgumentException.class,
            () -> new StaticUrlInfo(INVALID_ZONE_NAME, VALID_APPLICATION_URL, Stage.TEST));
        
        assertThrows(IllegalArgumentException.class,
            () -> new StaticUrlInfo(VALID_ZONE_NAME, INVALID_APPLICATION_URL, Stage.TEST));
    }
    
    @Test
    public void copyShouldReturnANewStaticUrlInfoInstanceWithNewStage() {
        StaticUrlInfo newUrlInfo = urlInfo.copy(Stage.FINAL);
        assertThat(newUrlInfo, is(not(equalTo(urlInfo))));
        assertThat(newUrlInfo.getStage(), is(equalTo(Stage.FINAL)));
    }
    
    @Test
    public void getRecordSetNameShouldReturnTheCorrectField() {
        assertThat(urlInfo.getRecordSetName(), is(equalTo(VALID_APPLICATION_URL)));
    }
    
    @Test
    public void getDomainNameShouldReturnTheCorrectField() {
        String expectedDomainName =
            VALID_APPLICATION_URL.substring(0, VALID_APPLICATION_URL.lastIndexOf(RECORDSET_NAME_LAST_CHARACTER));
        assertThat(urlInfo.getDomainName(), is(equalTo(expectedDomainName)));
    }
    
    @Test
    public void getZoneNameShouldReturnTheCorrectField() {
        assertThat(urlInfo.getZoneName(), is(equalTo(VALID_ZONE_NAME)));
    }
    
    @Test
    public void getStageShouldReturnTheCorrectField() {
        assertThat(urlInfo.getStage(), is(equalTo(Stage.TEST)));
    }
}
