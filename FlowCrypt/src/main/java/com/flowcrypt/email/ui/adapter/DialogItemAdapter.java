/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.flowcrypt.email.R;
import com.flowcrypt.email.model.DialogItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This adapter can be used with {@link DialogItem}
 *
 * @author Denis Bondarenko
 * Date: 01.08.2017
 * Time: 11:29
 * E-mail: DenBond7@gmail.com
 */

public class DialogItemAdapter extends BaseAdapter {
  private LayoutInflater inflater;
  private List<DialogItem> dialogItems;

  public DialogItemAdapter(Context context, List<DialogItem> dialogItems) {
    this.dialogItems = dialogItems;
    this.inflater = LayoutInflater.from(context);

    if (this.dialogItems == null) {
      this.dialogItems = new ArrayList<>();
    }
  }

  @Override
  public int getCount() {
    return dialogItems.size();
  }

  @Override
  public DialogItem getItem(int position) {
    return dialogItems.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    DialogItem dialogItem = getItem(position);

    ViewHolder viewHolder;
    if (convertView == null) {
      viewHolder = new ViewHolder();
      convertView = inflater.inflate(R.layout.dialog_item, parent, false);
      viewHolder.textViewItemTitle = convertView.findViewById(R.id.textViewDialogItem);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    viewHolder.textViewItemTitle.setText(dialogItem.getTitle());
    viewHolder.textViewItemTitle.setCompoundDrawablesWithIntrinsicBounds(dialogItem.getIconResourceId(), 0, 0, 0);

    return convertView;
  }

  private static class ViewHolder {
    TextView textViewItemTitle;
  }
}
