package io.droidninja.rxtodo;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.jakewharton.rxbinding2.widget.RxCompoundButton;

import org.reactivestreams.Subscription;

import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by Zeeshan Shabbir on 12/26/2017.
 */

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoHolder> implements Consumer<List<Todo>> {

    LayoutInflater inflater;

    Consumer<Todo> subscriber;

    TodoCompletedChangeListener todoChangeListener;

    List<Todo> data = Collections.EMPTY_LIST;

    public TodoAdapter(Activity activity,  Consumer<Todo> subscriber) {
        inflater = LayoutInflater.from(activity);
        this.subscriber = subscriber;
    }

    @Override
    public TodoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TodoHolder(inflater.inflate(R.layout.item_todo, parent, false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(TodoHolder holder, int position) {
        final Todo todo = data.get(position);
        holder.checkbox.setText(todo.description);
        holder.checkbox.setChecked(todo.isCompleted);
        holder.disposable = RxCompoundButton.checkedChanges(holder.checkbox).skip(1).map(new Function<Boolean, Todo>() {
            @Override
            public Todo apply(Boolean aBoolean) throws Exception {
                return todo;
            }
        }).subscribe(subscriber);
    }

    @Override
    public void onViewDetachedFromWindow(TodoHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.disposable.dispose();
    }

    @Override
    public void accept(List<Todo> todos) throws Exception {
        data = todos;
        notifyDataSetChanged();
    }

    public class TodoHolder extends RecyclerView.ViewHolder {

        public CheckBox checkbox;

        public Disposable disposable;

        public TodoHolder(View itemView) {
            super(itemView);
            checkbox = (CheckBox) itemView;
        }
    }
}
