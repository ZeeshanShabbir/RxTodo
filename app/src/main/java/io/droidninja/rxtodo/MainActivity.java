package io.droidninja.rxtodo;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;

import java.util.List;

import io.droidninja.rxtodo.databinding.ActivityMainBinding;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import static io.droidninja.rxtodo.TodoListFilter.COMPLETE;
import static io.droidninja.rxtodo.TodoListFilter.INCOMPLETE;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    private static final String LIST = "list";

    TodoList list;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (savedInstanceState != null) {
            list = new TodoList(savedInstanceState.getString(LIST));
        } else {
            list = new TodoList(getSharedPreferences("data", Context.MODE_PRIVATE).getString(LIST, null));
        }

        TodoAdapter adapter = new TodoAdapter(this, list);

        binding.rc.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.rc.setAdapter(adapter);

        binding.addTodoContainer.requestFocus(); // ensures the edittext isn't focused when entering the Activity
        compositeDisposable.add(RxView.clicks(binding.btnAddTodo)
                .map(new Function<Object, String>() {
                    @Override
                    public String apply(Object o) throws Exception {
                        return binding.addTodoInput.getText().toString();
                    }
                })
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return !TextUtils.isEmpty(s);
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        list.add(new Todo(s, false));
                        binding.addTodoInput.setText("");
                        binding.addTodoContainer.requestFocus();
                        dismissKeyboard();
                    }
                }));

        binding.spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"All", "Incomplete", "Completed"}));
        compositeDisposable.add(Observable.combineLatest(RxAdapterView.itemSelections(binding.spinner).skip(1),
                list.asObserable(),
                new BiFunction<Integer, TodoList, List<Todo>>() {
                    @Override
                    public List<Todo> apply(Integer integer, TodoList todoList) throws Exception {
                        switch (integer) {
                            case COMPLETE:
                                return todoList.getCompelete();
                            case INCOMPLETE:
                                return todoList.getIncomplete();
                            default:
                                return todoList.getAll();
                        }
                    }
                }).subscribe(adapter));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(LIST, list.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        editor.putString(LIST, list.toString());
        editor.apply();
        compositeDisposable.dispose();
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.addTodoInput.getWindowToken(), 0);
    }
}
