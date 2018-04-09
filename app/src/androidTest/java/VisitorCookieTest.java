import android.support.annotation.NonNull;
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
    final DiscoveryActivity activity = this.activityRule.getActivity();
    final Environment environment = activity.environment();

    final CookieManager cookieManager = environment.cookieManager();
    final CookieStore cookieStore = cookieManager.getCookieStore();
    final URI webUri = URI.create(Secrets.WebEndpoint.PRODUCTION);
    final URI apiUri = URI.create(ApiEndpoint.PRODUCTION.url());
    final Optional<HttpCookie> webVisCookie = getOptionalVisitorCookieForURI(cookieStore, webUri);
    final Optional<HttpCookie> apiVisCookie = getOptionalVisitorCookieForURI(cookieStore, apiUri);

    Assert.assertTrue(webVisCookie.isPresent());
    Assert.assertTrue(apiVisCookie.isPresent());

    final HttpCookie webCookie = webVisCookie.get();
    final HttpCookie apiCookie = apiVisCookie.get();

    final String deviceId = InstanceID.getInstance(activity).getId();
    Assert.assertNotNull(deviceId);

    Assert.assertTrue(webCookie.getValue().equals(deviceId));
    Assert.assertFalse(webCookie.getValue().equals("beep"));
    Assert.assertTrue(apiCookie.getValue().equals(deviceId));
    Assert.assertFalse(apiCookie.getValue().equals("boop"));
  }

  private Optional<HttpCookie> getOptionalVisitorCookieForURI(final @NonNull CookieStore cookieStore, final @NonNull URI uri) {
    return cookieStore.get(uri).stream().filter(c -> c.getName().equals(KEY_VIS)).findFirst();
  }
}
