package com.lemonclient.client.clickgui;

import com.lukflug.panelstudio.widget.ITextFieldKeys;

public class TextFieldKeys implements ITextFieldKeys {
   @Override
   public boolean isBackspaceKey(int scancode) {
      return scancode == 14;
   }

   @Override
   public boolean isDeleteKey(int scancode) {
      return scancode == 211;
   }

   @Override
   public boolean isInsertKey(int scancode) {
      return scancode == 210;
   }

   @Override
   public boolean isLeftKey(int scancode) {
      return scancode == 203;
   }

   @Override
   public boolean isRightKey(int scancode) {
      return scancode == 205;
   }

   @Override
   public boolean isHomeKey(int scancode) {
      return scancode == 199;
   }

   @Override
   public boolean isEndKey(int scancode) {
      return scancode == 207;
   }

   @Override
   public boolean isCopyKey(int scancode) {
      return scancode == 46;
   }

   @Override
   public boolean isPasteKey(int scancode) {
      return scancode == 47;
   }

   @Override
   public boolean isCutKey(int scancode) {
      return scancode == 45;
   }

   @Override
   public boolean isAllKey(int scancode) {
      return scancode == 30;
   }
}
