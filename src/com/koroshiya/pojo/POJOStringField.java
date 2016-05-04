package com.koroshiya.pojo;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;

import com.koroshiya.BR;

public class POJOStringField extends BaseObservable {

    @Bindable
    private final ObservableField<String> stringField = new ObservableField<>();

    public POJOStringField(String stringField){
        setStringField(stringField);
    }

    @Bindable
    public String getStringField() {
        return stringField.get();
    }

    public void setStringField(String stringField) {
        this.stringField.set(stringField);
        notifyPropertyChanged(BR.stringField);
    }

}
