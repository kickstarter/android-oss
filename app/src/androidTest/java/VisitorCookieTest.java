import com.google.firebase.iid.FirebaseInstanceId;
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
import java.net.URI;

import androidx.annotation.NonNull;
import androidx.test.filters.SmallTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

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

    final String deviceId = FirebaseInstanceId.getInstance().getId();
    Assert.assertNotNull(deviceId);

    Assert.assertTrue(hasVisitorCookieForURI(cookieStore, webUri, deviceId));
    Assert.assertTrue(hasVisitorCookieForURI(cookieStore, apiUri, deviceId));
  }

  private boolean hasVisitorCookieForURI(final @NonNull CookieStore cookieStore, final @NonNull URI uri, final @NonNull String deviceId) {
    return cookieStore.get(uri).stream().anyMatch(httpCookie -> httpCookie.getName().equals(KEY_VIS) && httpCookie.getValue().equals(deviceId));
  }
}
