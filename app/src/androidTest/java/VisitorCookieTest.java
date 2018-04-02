import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.iid.InstanceID;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.ui.activities.DiscoveryActivity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Optional;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class VisitorCookieTest {

  private static final String KEY_VIS = "vis";

  @Rule
  public ActivityTestRule<DiscoveryActivity> activityRule =
    new ActivityTestRule<>(DiscoveryActivity.class);

  @Test
  public void testVisitorCookieHasBeenSet() {
    final DiscoveryActivity activity = activityRule.getActivity();
    final Environment environment = activity.environment();

    final CookieManager cookieManager = environment.cookieManager();
    final CookieStore cookieStore = cookieManager.getCookieStore();
    final URI webUri = URI.create(Secrets.WebEndpoint.PRODUCTION);
    final URI apiUri = URI.create(ApiEndpoint.PRODUCTION.url());
    final Optional<HttpCookie> webVisCookie = getVisitorCookieForURI(cookieStore, webUri);
    final Optional<HttpCookie> apiVisCookie = getVisitorCookieForURI(cookieStore, apiUri);

    final String deviceId = InstanceID.getInstance(activity).getId();

    Assert.assertNotNull(deviceId);
    Assert.assertNotNull(webVisCookie.get());
    Assert.assertNotNull(apiVisCookie.get());
    Assert.assertTrue(webVisCookie.get().getValue().equals(deviceId));
    Assert.assertTrue(apiVisCookie.get().getValue().equals(deviceId));
  }
  private Optional<HttpCookie> getVisitorCookieForURI(CookieStore cookieStore, URI webUri) {
    // it's problematic
    return cookieStore.get(webUri).stream().filter(c -> c.getName().equals(KEY_VIS)).findFirst();
  }
}
