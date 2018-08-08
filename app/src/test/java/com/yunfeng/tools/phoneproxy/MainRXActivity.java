package com.yunfeng.tools.phoneproxy;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * rxandroid
 * Created by xll on 2018/8/7.
 */
public class MainRXActivity extends Activity {

    Button mSearchButton;
    EditText mQueryEditText;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_rxandroid);
//        Schedulers.io(): 适合I/O类型的操作，比如网络请求，磁盘操作。
//        Schedulers.computation(): 适合计算任务，比如事件循环或者回调处理。
//        AndroidSchedulers.mainThread() : 回调主线程，比如UI操作。
//        myObservable // observable will be subscribed on i/o thread
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(/* this will be called on main thread... */)
//                .doOnNext(/* ...and everything below until next observeOn */)
//                .observeOn(Schedulers.io())
//                .subscribe(/* this will be called on i/o thread */);
        mSearchButton = findViewById(R.id.btn_search);
        mQueryEditText = findViewById(R.id.et_edit);
        mProgressBar = findViewById(R.id.progressBar2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Observable<String> searchTextObservable = createButtonClickObservable();

        final Consumer<String> consumerStart = new Consumer<String>() {
            @Override
            public void accept(String s) {
                showProgressBar();
            }
        };
        final Function<String, List<String>> mapper = new Function<String, List<String>>() {
            @Override
            public List<String> apply(String query) {
                return search(query);
            }
        };
        final Consumer<List<String>> consumerEnd = new Consumer<List<String>>() {
            @Override
            public void accept(List<String> result) {
                // 3
                hideProgressBar();
//                showResult(result);
            }
        };
        searchTextObservable.observeOn(AndroidSchedulers.mainThread()).doOnNext(consumerStart).observeOn(Schedulers.io()).map(mapper).observeOn(AndroidSchedulers.mainThread()).subscribe(consumerEnd);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private Observable<String> createButtonClickObservable() {
        final Observable<String> stringObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                // 4
                mSearchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        emitter.onNext(mQueryEditText.getText().toString());
                    }
                });

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        mSearchButton.setOnClickListener(null);
                    }
                });
            }
        });
        return stringObservable;
    }

    private List<String> search(String query) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(4);
    }
}
