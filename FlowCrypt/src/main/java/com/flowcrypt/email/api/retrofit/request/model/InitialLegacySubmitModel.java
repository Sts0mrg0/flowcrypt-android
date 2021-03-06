/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.api.retrofit.request.model;

import android.os.Parcel;

import com.google.gson.annotations.Expose;

/**
 * This is a POJO object which used to make a request
 * to the API "https://flowcrypt.com/api/initial/legacy_submit"
 *
 * @author DenBond7
 * Date: 15.01.2018
 * Time: 16:37
 * E-mail: DenBond7@gmail.com
 */

public class InitialLegacySubmitModel extends BaseRequestModel {

  public static final Creator<InitialLegacySubmitModel> CREATOR = new Creator<InitialLegacySubmitModel>() {
    @Override
    public InitialLegacySubmitModel createFromParcel(Parcel source) {
      return new InitialLegacySubmitModel(source);
    }

    @Override
    public InitialLegacySubmitModel[] newArray(int size) {
      return new InitialLegacySubmitModel[size];
    }
  };

  @Expose
  private String email;

  @Expose
  private String pubkey;

  /*todo-denbond7 Make sure to choose attest: false for now.
   https://github.com/FlowCrypt/flowcrypt-android/issues/71*/
  @Expose
  private boolean attest = false;

  public InitialLegacySubmitModel() {
  }

  public InitialLegacySubmitModel(String email, String pubkey) {
    this.email = email;
    this.pubkey = pubkey;
  }

  protected InitialLegacySubmitModel(Parcel in) {
    this.email = in.readString();
    this.pubkey = in.readString();
    this.attest = in.readByte() != 0;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.email);
    dest.writeString(this.pubkey);
    dest.writeByte(this.attest ? (byte) 1 : (byte) 0);
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPubkey() {
    return pubkey;
  }

  public void setPubkey(String pubkey) {
    this.pubkey = pubkey;
  }

  public boolean isAttest() {
    return attest;
  }

  public void setAttest(boolean attest) {
    this.attest = attest;
  }
}
