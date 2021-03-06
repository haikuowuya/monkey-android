package com.yeungeek.monkeyandroid.ui.detail;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yeungeek.monkeyandroid.R;
import com.yeungeek.monkeyandroid.data.model.Repo;
import com.yeungeek.monkeyandroid.ui.base.view.BaseActivity;
import com.yeungeek.monkeyandroid.ui.base.view.BaseLceFragment;
import com.yeungeek.monkeyandroid.util.AppCst;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by yeungeek on 2016/4/10.
 */
public class RepoDetailFragment extends BaseLceFragment<View, String, RepoDetailMvpView, RepoDetailPresenter> implements RepoDetailMvpView {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.id_repo_owner_avatar)
    ImageView mAvatar;
    @Bind(R.id.id_repo_name)
    TextView mRepoName;
    @Bind(R.id.id_repo_stars)
    TextView mStar;
    @Bind(R.id.id_repo_fork)
    TextView mFork;
    @Bind(R.id.id_repo_desc)
    TextView mRepoDesc;
    @Bind(R.id.id_repo_detail)
    WebView mRepoDetail;
    @Bind(R.id.id_repo_star)
    FloatingActionButton mRepoStar;

    @Inject
    RepoDetailPresenter repoDetailPresenter;

    private ActionBar actionBar;

    private Repo mRepo;
    private String mStarString;
    private String mForkString;
    private boolean mCurrentStaring;

    public static Fragment newInstance(final Context context, final Repo repo) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppCst.EXTRA_REPO, repo);
        return Fragment.instantiate(context, RepoDetailFragment.class.getName(), bundle);
    }

    @Override
    protected void init() {
        super.init();
        mRepo = (Repo) getArguments().getSerializable(AppCst.EXTRA_REPO);
        mStarString = getString(R.string.title_star);
        mForkString = getString(R.string.title_fork);
    }

    @Override
    protected void initViews() {
        super.initViews();
        if (null == mRepo) {
            return;
        }

        if (null != toolbar) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (null != actionBar) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                actionBar.setTitle(mRepo.getName());
            }
        }

        mRepoName.setText(mRepo.getFull_name());
        mStar.setText(String.format(mStarString, mRepo.getStargazers_count()));
        mFork.setText(String.format(mForkString, mRepo.getForks_count()));
        mRepoDesc.setText(mRepo.getDescription());

        if (null != mRepo.getOwner()) {
            Glide.with(this).load(mRepo.getOwner().getAvatarUrl()).into(mAvatar);
        }

        initWebView();
        loadData(false);
        checkStarStatus();
    }

    private void initWebView() {
        mRepoDetail.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_repo_detail;
    }

    @Override
    protected String getErrorMessage(Throwable e, boolean pullToRefresh) {
        return e.getMessage();
    }

    @Override
    public RepoDetailPresenter createPresenter() {
        return repoDetailPresenter;
    }

    @Override
    public void setData(String data) {
        mRepoDetail.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        if (null == mRepo) {
            return;
        }

        getPresenter().getReadme(mRepo.getOwner().getLogin(), mRepo.getName(), false);
    }

    @Override
    public void notLogined() {
        Toast.makeText(getContext(), getString(R.string.error_not_login), Toast.LENGTH_SHORT).show();
    }

    private void checkStarStatus() {
        if (getPresenter().isLogined()) {
            getPresenter().checkIfStaring(mRepo);
        }
    }

    @Override
    protected void injectDependencies() {
        super.injectDependencies();
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).activityComponent().inject(this);
        }
    }

    @Override
    public void starStatus(boolean isStaring) {
        mCurrentStaring = isStaring;
        if (isStaring) {
            mRepoStar.setImageResource(R.drawable.ic_favorite);
        } else {
            mRepoStar.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    @OnClick(R.id.id_repo_star)
    public void onFabClick() {
        if (!getPresenter().checkLogin()) {
            return;
        }

        if (mCurrentStaring) {
            getPresenter().unstarRepo(mRepo);
        } else {
            getPresenter().starRepo(mRepo);
        }
    }
}
