package com.kickstarter.viewmodels;

import android.content.Intent;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.mock.factories.ApiExceptionFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UpdateFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.data.CommentsData;

import org.junit.Test;

import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.observers.TestSubscriber;

public class CommentsViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testCommentsViewModel_EmptyState() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<CommentsEnvelope> fetchComments(final @NonNull Update update) {
        return Observable.empty();
      }
    };

    final Environment env = environment().toBuilder().apiClient(apiClient).build();
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(env);

    final TestSubscriber<CommentsData> commentsData = new TestSubscriber<>();
    vm.outputs.commentsData().subscribe(commentsData);

    // Start the view model with an update.
    vm.intent(new Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()));

    commentsData.assertNoValues();
  }

  @Test
  public void testCommentsViewModel_ProjectCommentsEmit() {
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(environment());

    final TestSubscriber<List<Comment>> comments = new TestSubscriber<>();
    vm.outputs.commentsData().map(CommentsData::comments).subscribe(comments);

    final TestSubscriber<Boolean> isFetchingComments = new TestSubscriber<>();
    vm.outputs.isFetchingComments().subscribe(isFetchingComments);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()));

    // Comments should emit.
    comments.assertValueCount(1);
    isFetchingComments.assertValues(true, false);
  }

  @Test
  public void testCommentsViewModel_postCommentError() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<Comment> postComment(final @NonNull Project project, final @NonNull String body) {
        return Observable.error(ApiExceptionFactory.badRequestException());
      }
    };

    final Environment env = environment().toBuilder().apiClient(apiClient).build();
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(env);

    final TestSubscriber<String> showPostCommentErrorToast = new TestSubscriber<>();
    vm.outputs.showPostCommentErrorToast().subscribe(showPostCommentErrorToast);

    final TestSubscriber<Void> showCommentPostedToast = new TestSubscriber<>();
    vm.outputs.showCommentPostedToast().subscribe(showCommentPostedToast);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()));

    // Click the comment button and write a comment.
    vm.inputs.commentButtonClicked();
    vm.inputs.commentBodyChanged("Mic check mic check.");

    // Post comment. Error should be shown. Comment posted toast should not be shown.
    vm.inputs.postCommentClicked();
    showPostCommentErrorToast.assertValueCount(1);
    showCommentPostedToast.assertNoValues();
  }

  @Test
  public void testCommentsViewModel_postCommentFlow() {
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(
      environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    final Project project = ProjectFactory.backedProject();

    final TestSubscriber<Void> showCommentPostedToastTest = new TestSubscriber<>();
    vm.outputs.showCommentPostedToast().subscribe(showCommentPostedToastTest);

    final TestSubscriber<Void> dismissCommentDialogTest = new TestSubscriber<>();
    vm.outputs.dismissCommentDialog().subscribe(dismissCommentDialogTest);

    final TestSubscriber<Boolean> postButtonIsEnabledTest = new TestSubscriber<>();
    vm.outputs.enablePostButton().subscribe(postButtonIsEnabledTest);

    final TestSubscriber<Boolean> commentButtonHidden = new TestSubscriber<>();
    vm.outputs.commentButtonHidden().subscribe(commentButtonHidden);

    final TestSubscriber<Pair<Project, Boolean>> showCommentDialogTest = new TestSubscriber<>();
    vm.outputs.showCommentDialog().subscribe(showCommentDialogTest);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // Comment button should be shown.
    commentButtonHidden.assertValue(false);

    // Click comment button. Comment dialog should be shown.
    vm.inputs.commentButtonClicked();
    showCommentDialogTest.assertValue(Pair.create(project, true));

    // Write a comment. The post button should be enabled with valid comment body.
    vm.inputs.commentBodyChanged("");
    postButtonIsEnabledTest.assertValues(false);
    vm.inputs.commentBodyChanged("Some comment");
    postButtonIsEnabledTest.assertValues(false, true);

    // Post comment. Dialog should be dismissed.
    vm.inputs.postCommentClicked();
    dismissCommentDialogTest.assertValueCount(1);

    // Comment posted toast should be shown.
    showCommentPostedToastTest.assertValueCount(1);
  }

  @Test
  public void testCommentsViewModel_loggedOutShowDialogFlow() {
    final CurrentUserType currentUser = new MockCurrentUser(UserFactory.user());
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(
      environment().toBuilder().currentUser(currentUser).build()
    );

    final Project project = ProjectFactory.backedProject();

    final TestSubscriber<Pair<Project, Boolean>> showCommentDialogTest = new TestSubscriber<>();
    vm.outputs.showCommentDialog().subscribe(showCommentDialogTest);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // The comment dialog should be hidden from logged out user.
    showCommentDialogTest.assertNoValues();

    // Login.
    currentUser.refresh(UserFactory.user());
    vm.inputs.loginSuccess();

    // The comment dialog should be shown to backer.
    showCommentDialogTest.assertValue(Pair.create(project, true));
  }

  @Test
  public void testCommentsViewModel_showCommentButton_isBacking() {
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(
      environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    final TestSubscriber<Boolean> commentButtonHidden = new TestSubscriber<>();
    vm.outputs.commentButtonHidden().subscribe(commentButtonHidden);

    // Start the view model with a backed project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()));

    // The comment button should be shown to backer.
    commentButtonHidden.assertValue(false);
  }

  @Test
  public void testCommentsViewModel_commentButtonShown_isCreator() {
    final User currentUser = UserFactory.user().toBuilder().id(1234).build();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .creator(currentUser)
      .isBacking(false)
      .build();

    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(
      environment().toBuilder().currentUser(new MockCurrentUser(currentUser)).build()
    );

    final TestSubscriber<Boolean> commentButtonHidden = new TestSubscriber<>();
    vm.outputs.commentButtonHidden().subscribe(commentButtonHidden);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // Comment button is shown for the creator.
    commentButtonHidden.assertValues(false);
  }

  @Test
  public void testCommentsViewModel_commentButtonHidden_notBackingNotCreator() {
    final User creator = UserFactory.creator().toBuilder().id(222).build();
    final User currentUser = UserFactory.user().toBuilder().id(111).build();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .creator(creator)
      .isBacking(false)
      .build();

    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(
      environment().toBuilder().currentUser(new MockCurrentUser(currentUser)).build()
    );

    final TestSubscriber<Boolean> commentButtonHidden = new TestSubscriber<>();
    vm.outputs.commentButtonHidden().subscribe(commentButtonHidden);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // Comment button should be hidden if not backing and not creator.
    commentButtonHidden.assertValue(true);
  }

  @Test
  public void testCommentsViewModel_dismissCommentDialog() {
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(environment());

    final TestSubscriber<Void> dismissCommentDialogTest = new TestSubscriber<>();
    vm.outputs.dismissCommentDialog().subscribe(dismissCommentDialogTest);

    final TestSubscriber<Pair<Project, Boolean>> showCommentDialogTest = new TestSubscriber<>();
    vm.outputs.showCommentDialog().subscribe(showCommentDialogTest);

    final Project project = ProjectFactory.backedProject();

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // The comment dialog should not be shown.
    showCommentDialogTest.assertNoValues();
    dismissCommentDialogTest.assertNoValues();

    // Dismiss the comment dialog.
    vm.inputs.commentDialogDismissed();

    // The comment dialog should be dismissed.
    dismissCommentDialogTest.assertValueCount(1);
    showCommentDialogTest.assertValue(null);
  }

  @Test
  public void testCommentsViewModel_currentCommentBody() {
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(environment());

    final TestSubscriber<String> currentCommentBodyTest = new TestSubscriber<>();
    vm.outputs.currentCommentBody().subscribe(currentCommentBodyTest);

    currentCommentBodyTest.assertNoValues();

    vm.inputs.commentBodyChanged("Hello");
    currentCommentBodyTest.assertValues("Hello");
  }

  @Test
  public void testCommentsViewModel_showCommentDialog() {
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(environment());

    final TestSubscriber<Pair<Project, Boolean>> showCommentDialogTest = new TestSubscriber<>();
    vm.outputs.showCommentDialog().subscribe(showCommentDialogTest);

    final Project project = ProjectFactory.backedProject();

    // Start the view model with a backed project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    showCommentDialogTest.assertNoValues();

    // Click the comment button.
    vm.inputs.commentButtonClicked();

    // The comment dialog should be shown.
    showCommentDialogTest.assertValue(Pair.create(project, true));
  }

  @Test
  public void testCommentsViewModel_UpdateCommentsEmit() {
    final CommentsViewModel.ViewModel vm = new CommentsViewModel.ViewModel(environment());

    final TestSubscriber<CommentsData> commentsData = new TestSubscriber<>();
    vm.outputs.commentsData().subscribe(commentsData);

    // Start the view model with an update.
    vm.intent(new Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()));

    // Comments should emit.
    commentsData.assertValueCount(1);
  }
}
