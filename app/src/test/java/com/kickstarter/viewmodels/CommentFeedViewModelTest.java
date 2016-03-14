package com.kickstarter.viewmodels;

import android.content.Intent;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class CommentFeedViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testCommentFeedViewModel_postCommentFlow() {
    final CommentFeedViewModel vm = new CommentFeedViewModel(environment());
    final Project project = ProjectFactory.backedProject();

    final TestSubscriber<Void> commentPostedTest = new TestSubscriber<>();
    vm.outputs.commentPosted().subscribe(commentPostedTest);

    final TestSubscriber<Boolean> postButtonIsEnabledTest = new TestSubscriber<>();
    vm.outputs.postButtonIsEnabled().subscribe(postButtonIsEnabledTest);

    final TestSubscriber<Boolean> showCommentButtonTest = new TestSubscriber<>();
    vm.outputs.showCommentButton().subscribe(showCommentButtonTest);

    final TestSubscriber<Pair<Project, Boolean>> showCommentDialogTest = new TestSubscriber<>();
    vm.outputs.showCommentDialog().subscribe(showCommentDialogTest);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    koalaTest.assertValues("Project Comment View");

    // Comment button should be shown.
    showCommentButtonTest.assertValue(true);

    // Click comment button. Comment dialog should be shown.
    vm.inputs.commentButtonClicked();
    showCommentDialogTest.assertValue(Pair.create(project, true));

    // Write a comment. The post button should be enabled with valid comment body.
    vm.inputs.commentBody("");
    postButtonIsEnabledTest.assertValues(false);
    vm.inputs.commentBody("Some comment");
    postButtonIsEnabledTest.assertValues(false, true);

    // Post comment. Dialog should be dismissed.
    vm.inputs.postCommentClicked();
    showCommentDialogTest.assertValues(Pair.create(project, true), Pair.create(project, false));

    // Comment should be posted.
    commentPostedTest.assertValueCount(1);

    // A koala event for commenting should be tracked.
    koalaTest.assertValues("Project Comment View", "Project Comment Create");
  }

  @Test
  public void testCommentFeedViewModel_commentButtonHidden() {
    final CommentFeedViewModel vm = new CommentFeedViewModel(environment());

    final TestSubscriber<Boolean> showCommentButtonTest = new TestSubscriber<>();
    vm.outputs.showCommentButton().subscribe(showCommentButtonTest);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()));
    koalaTest.assertValues("Project Comment View");

    // Comment button should not be shown if not backing.
    showCommentButtonTest.assertValue(false);
  }

  @Test
  public void testCommentFeedViewModel_dismissCommentDialog() {
    final CommentFeedViewModel vm = new CommentFeedViewModel(environment());

    final TestSubscriber<Pair<Project, Boolean>> showCommentDialogTest = new TestSubscriber<>();
    vm.outputs.showCommentDialog().subscribe(showCommentDialogTest);

    final Project project = ProjectFactory.backedProject();

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    koalaTest.assertValues("Project Comment View");

    // The comment dialog should not be shown.
    showCommentDialogTest.assertNoValues();

    // Dismiss the comment dialog.
    vm.inputs.dismissCommentDialog();

    // The comment dialog should be dismissed.
    showCommentDialogTest.assertValue(Pair.create(project, false));
  }

  @Test
  public void testCommentFeedViewModel_loggedOutShowDialogFlow() {
    final CurrentUserType currentUser = new MockCurrentUser();

    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final CommentFeedViewModel vm = new CommentFeedViewModel(environment);

    final Project backedProjectWithNoComments = ProjectFactory.backedProject()
      .toBuilder()
      .commentsCount(0)
      .build();

    final TestSubscriber<Pair<Project, Boolean>> showCommentDialogTest = new TestSubscriber<>();
    vm.outputs.showCommentDialog().subscribe(showCommentDialogTest);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, backedProjectWithNoComments));
    koalaTest.assertValues("Project Comment View");

    // The comment dialog should be hidden from logged out user.
    showCommentDialogTest.assertNoValues();

    // Login.
    currentUser.refresh(UserFactory.user());
    vm.inputs.loginSuccess();

    // The comment dialog should be shown to backer.
    showCommentDialogTest.assertValue(Pair.create(backedProjectWithNoComments, true));
  }


  @Test
  public void testCommentFeedViewModel_initialCommentBody() {
    final CommentFeedViewModel vm = new CommentFeedViewModel(environment());

    final TestSubscriber<String> initialCommentBodyTest = new TestSubscriber<>();
    vm.outputs.initialCommentBody().subscribe(initialCommentBodyTest);

    initialCommentBodyTest.assertNoValues();
    koalaTest.assertValues("Project Comment View");

    vm.inputs.commentBody("Hello");
    initialCommentBodyTest.assertValues("Hello");
  }

  @Test
  public void testCommentFeedViewModel_showCommentButton() {
    final CommentFeedViewModel vm = new CommentFeedViewModel(environment());

    final TestSubscriber<Boolean> showCommentButtonTest = new TestSubscriber<>();
    vm.outputs.showCommentButton().subscribe(showCommentButtonTest);

    // Start the view model with a backed project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()));
    koalaTest.assertValues("Project Comment View");

    // The comment button should be shown to backer.
    showCommentButtonTest.assertValue(true);
  }

  @Test
  public void testCommentFeedViewModel_showCommentDialog() {
    final CommentFeedViewModel vm = new CommentFeedViewModel(environment());

    final TestSubscriber<Pair<Project, Boolean>> showCommentDialogTest = new TestSubscriber<>();
    vm.outputs.showCommentDialog().subscribe(showCommentDialogTest);

    final Project project = ProjectFactory.backedProject();

    // Start the view model with a backed project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    koalaTest.assertValues("Project Comment View");

    showCommentDialogTest.assertNoValues();

    // Click the comment button.
    vm.inputs.commentButtonClicked();

    // The comment dialog should be shown.
    showCommentDialogTest.assertValue(Pair.create(project, true));
  }
}
