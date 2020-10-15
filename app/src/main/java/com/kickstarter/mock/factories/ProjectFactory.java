package com.kickstarter.mock.factories;

import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.User;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import type.CreditCardTypes;

public final class ProjectFactory {
  private ProjectFactory() {}

  public static @NonNull Project project() {
    final User creator = UserFactory.creator();
    final String slug = "slug-1";
    final String projectUrl = "https://www.kickstarter.com/projects/" + String.valueOf(creator.id()) + "/" + slug;

    final Project.Urls.Web web = Project.Urls.Web.builder()
      .project(projectUrl)
      .rewards(projectUrl + "/rewards")
      .updates(projectUrl + "/posts")
      .build();

    return Project.builder()
      .availableCardTypes(Arrays.asList(CreditCardTypes.AMEX.rawValue(),
        CreditCardTypes.DINERS.rawValue(),
        CreditCardTypes.DISCOVER.rawValue(),
        CreditCardTypes.JCB.rawValue(),
        CreditCardTypes.MASTERCARD.rawValue(),
        CreditCardTypes.UNION_PAY.rawValue(),
        CreditCardTypes.VISA.rawValue()))
      .backersCount(100)
      .blurb("Some blurb")
      .category(CategoryFactory.category())
      .creator(UserFactory.creator())
      .country("US")
      .createdAt(DateTime.now(DateTimeZone.UTC))
      .currency("USD")
      .currencySymbol("$")
      .currentCurrency("USD")
      .currencyTrailingCode(true)
      .fxRate(1.0f)
      .goal(100.0f)
      .id(IdFactory.id())
      .location(LocationFactory.unitedStates())
      .name("Some Name")
      .pledged(50.0f)
      .photo(PhotoFactory.photo())
      .rewards(Arrays.asList(RewardFactory.noReward(), RewardFactory.reward()))
      .staffPick(false)
      .state(Project.STATE_LIVE)
      .staticUsdRate(1.0f)
      .slug(slug)
      .updatedAt(DateTime.now())
      .urls(Project.Urls.builder().web(web).build())
      .video(VideoFactory.video())
      .launchedAt(new DateTime(DateTimeZone.UTC).minusDays(10))
      .deadline(new DateTime(DateTimeZone.UTC).plusDays(10))
      .build();
  }

  public static @NonNull Project backedProject() {
    final Project project = project();

    final Reward reward = RewardFactory.reward();

    final Backing backing = Backing.builder()
      .amount(10.0f)
      .backerId(IdFactory.id())
      .cancelable(true)
      .id(IdFactory.id())
      .sequence(1)
      .reward(reward)
      .rewardId(reward.id())
      .paymentSource(PaymentSourceFactory.Companion.visa())
      .pledgedAt(DateTime.now())
      .projectId(project.id())
      .shippingAmount(0.0f)
      .status(Backing.STATUS_PLEDGED)
      .build();

    return project
      .toBuilder()
      .backing(backing)
      .isBacking(true)
      .build();
  }

  public static @NonNull Project backedProjectWithRewardAndAddOnsLimitReached() {
    final Project project = project();

    final Reward reward = RewardFactory.reward().toBuilder().hasAddons(true).limit(10).build();
    final Reward add1 = RewardFactory.addOn()
            .toBuilder()
            .remaining(0)
            .limit(0)
            .quantity(1)
            .build();

    final List<Reward> addOns = new ArrayList<Reward>();
    addOns.add(add1);

    final Backing backing = Backing.builder()
            .amount(10.0f)
            .backerId(IdFactory.id())
            .addOns(addOns)
            .cancelable(true)
            .id(IdFactory.id())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .paymentSource(PaymentSourceFactory.Companion.visa())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build();

    return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build();
  }

  public static @NonNull Project backedProjectWithAddOns() {
    final Project project = project();

    final Reward reward = RewardFactory.reward().toBuilder().hasAddons(true).build();
    final Reward add1 = RewardFactory.addOn();

    final List<Reward> addOns = new ArrayList<Reward>();
    addOns.add(add1);
    addOns.add(add1);

    final Backing backing = Backing.builder()
            .amount(10.0f)
            .backerId(IdFactory.id())
            .addOns(addOns)
            .cancelable(true)
            .id(IdFactory.id())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .paymentSource(PaymentSourceFactory.Companion.visa())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build();

    return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build();
  }


  public static @NonNull Project backedSuccessfulProject() {
    final Project project = successfulProject();

    final Reward reward = RewardFactory.reward();

    final Backing backing = Backing.builder()
      .amount(10.0f)
      .backerId(IdFactory.id())
      .cancelable(true)
      .id(IdFactory.id())
      .sequence(1)
      .reward(reward)
      .rewardId(reward.id())
      .pledgedAt(DateTime.now())
      .projectId(project.id())
      .shippingAmount(0.0f)
      .status(Backing.STATUS_PLEDGED)
      .build();

    return project
      .toBuilder()
      .backing(backing)
      .isBacking(true)
      .build();
  }

  public static @NonNull Project backedProjectWithRewardLimited() {
    final Project project = project();

    final Reward reward = RewardFactory.limited();

    final Backing backing = Backing.builder()
      .amount(10.0f)
      .backerId(IdFactory.id())
      .cancelable(true)
      .id(IdFactory.id())
      .sequence(1)
      .reward(reward)
      .rewardId(reward.id())
      .pledgedAt(DateTime.now())
      .projectId(project.id())
      .shippingAmount(0.0f)
      .status(Backing.STATUS_PLEDGED)
      .build();

    return project
      .toBuilder()
      .backing(backing)
      .isBacking(true)
      .build();
  }

  public static @NonNull Project backedProjectWithRewardLimitReached() {
    final Project project = project();

    final Reward reward = RewardFactory.limitReached();

    final Backing backing = Backing.builder()
      .amount(10.0f)
      .backerId(IdFactory.id())
      .cancelable(true)
      .id(IdFactory.id())
      .sequence(1)
      .reward(reward)
      .rewardId(reward.id())
      .pledgedAt(DateTime.now())
      .projectId(project.id())
      .shippingAmount(0.0f)
      .status(Backing.STATUS_PLEDGED)
      .build();

    return project
      .toBuilder()
      .backing(backing)
      .isBacking(true)
      .build();
  }

  public static @NonNull Project backedProjectWithNoReward() {
    final Project project = project();

    final Reward reward = RewardFactory.noReward();

    final Backing backing = Backing.builder()
      .amount(10.0f)
      .backerId(IdFactory.id())
      .cancelable(true)
      .id(IdFactory.id())
      .sequence(1)
      .reward(reward)
      .rewardId(null)
      .pledgedAt(DateTime.now())
      .projectId(project.id())
      .shippingAmount(0.0f)
      .status(Backing.STATUS_PLEDGED)
      .build();

    return project
      .toBuilder()
      .backing(backing)
      .isBacking(true)
      .build();
  }

  public static @NonNull Project halfWayProject() {
    return project()
      .toBuilder()
      .name("halfwayProject")
      .goal(100.0f)
      .pledged(50.0f)
      .build();
  }

  public static @NonNull Project allTheWayProject() {
    return project()
      .toBuilder()
      .name("allTheWayProject")
      .goal(100.0f)
      .pledged(100.0f)
      .build();
  }

  public static @NonNull Project doubledGoalProject() {
    return project()
      .toBuilder()
      .name("doubledGoalProject")
      .goal(100.0f)
      .pledged(200.0f)
      .build();
  }

  public static @NonNull Project failedProject() {
    return project()
      .toBuilder()
      .name("failedProject")
      .state(Project.STATE_FAILED)
      .build();
  }

  public static @NonNull Project caProject() {
    return project()
      .toBuilder()
      .availableCardTypes(Arrays.asList(CreditCardTypes.AMEX.rawValue(),
        CreditCardTypes.MASTERCARD.rawValue(),
        CreditCardTypes.VISA.rawValue()))
      .name("caProject")
      .country("CA")
      .currentCurrency("CAD")
      .currencySymbol("$")
      .currency("CAD")
      .staticUsdRate(0.75f)
      .fxRate(0.75f)
      .build();
  }

  public static @NonNull Project mxCurrencyCAProject() {
    return project()
      .toBuilder()
      .availableCardTypes(Arrays.asList(CreditCardTypes.AMEX.rawValue(),
        CreditCardTypes.MASTERCARD.rawValue(),
        CreditCardTypes.VISA.rawValue()))
      .name("mxCurrencyCAProject")
      .country("CA")
      .currentCurrency("MXN")
      .currencySymbol("$")
      .currency("CAD")
      .staticUsdRate(0.75f)
      .fxRate(.75f)
      .build();
  }


  public static @NonNull Project mxProject() {
    return project()
      .toBuilder()
      .availableCardTypes(Arrays.asList(CreditCardTypes.AMEX.rawValue(),
        CreditCardTypes.MASTERCARD.rawValue(),
        CreditCardTypes.VISA.rawValue()))
      .name("mxProject")
      .country("MX")
      .currentCurrency("MXN")
      .currencySymbol("$")
      .currency("MXN")
      .location(LocationFactory.mexico())
      .staticUsdRate(0.75f)
      .fxRate(0.75f)
      .build();
  }

  public static @NonNull Project ukProject() {
    return project()
      .toBuilder()
      .availableCardTypes(Arrays.asList(CreditCardTypes.AMEX.rawValue(),
        CreditCardTypes.MASTERCARD.rawValue(),
        CreditCardTypes.VISA.rawValue()))
      .name("ukProject")
      .country("UK")
      .currentCurrency("GBP")
      .currencySymbol("£")
      .currency("GBP")
      .staticUsdRate(1.5f)
      .fxRate(1.5f)
      .build();
  }

  public static @NonNull Project almostCompletedProject() {
    return project()
      .toBuilder()
      .name("almostCompleteProject")
      .deadline(new DateTime().plusDays(1))
      .build();
  }

  public static @NonNull Project successfulProject() {
    return project()
      .toBuilder()
      .name("successfulProject")
      .deadline(new DateTime().minus(2))
      .state(Project.STATE_SUCCESSFUL)
      .build();
  }

  public static Project featured() {
    return project()
      .toBuilder()
      .name("featuredProject")
      .featuredAt(new DateTime())
      .build();
  }

  public static Project saved() {
    return project()
      .toBuilder()
      .name("savedProject")
      .isStarred(true)
      .build();
  }

  public static Project staffPick() {
    return project()
      .toBuilder()
      .name("staffPickProject")
      .staffPick(true)
      .build();
  }

  public static Project prelaunchProject(final String projectUrl) {

    final Project.Urls.Web web = Project.Urls.Web.builder()
      .project(projectUrl)
      .rewards(projectUrl + "/rewards")
      .updates(projectUrl + "/posts")
      .build();

    return project()
      .toBuilder()
      .prelaunchActivated(true)
      .urls(Project.Urls.builder().web(web).build())
      .build();
  }

  public static Project initialProject() {
    return project()
      .toBuilder()
      .rewards(null)
      .build();
  }
}
