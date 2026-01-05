package com.lemonclient.api.util.player.social;

import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.qwq.Friends;
import java.util.ArrayList;

public class SocialManager {
   private static final ArrayList<Friend> friends = new ArrayList<>();
   private static final ArrayList<Enemy> enemies = new ArrayList<>();
   private static final ArrayList<Ignore> ignores = new ArrayList<>();

   public static ArrayList<Friend> getFriends() {
      return friends;
   }

   public static ArrayList<Enemy> getEnemies() {
      return enemies;
   }

   public static ArrayList<Ignore> getIgnores() {
      return ignores;
   }

   public static ArrayList<String> getFriendsByName() {
      ArrayList<String> friendNames = new ArrayList<>();
      getFriends().forEach(friend -> friendNames.add(friend.getName()));
      return friendNames;
   }

   public static ArrayList<String> getEnemiesByName() {
      ArrayList<String> enemyNames = new ArrayList<>();
      getEnemies().forEach(enemy -> enemyNames.add(enemy.getName()));
      return enemyNames;
   }

   public static ArrayList<String> getIgnoresByName() {
      ArrayList<String> ignoreNames = new ArrayList<>();
      getIgnores().forEach(ignore -> ignoreNames.add(ignore.getName()));
      return ignoreNames;
   }

   public static boolean isFriend(String name) {
      for (Friend friend : getFriends()) {
         if (friend.getName().equalsIgnoreCase(name) && ModuleManager.isModuleEnabled(Friends.class)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isOnFriendList(String name) {
      boolean value = false;

      for (Friend friend : getFriends()) {
         if (friend.getName().equalsIgnoreCase(name)) {
            value = true;
            break;
         }
      }

      return value;
   }

   public static boolean isEnemy(String name) {
      for (Enemy enemy : getEnemies()) {
         if (enemy.getName().equalsIgnoreCase(name)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isOnEnemyList(String name) {
      boolean value = false;

      for (Enemy enemy : getEnemies()) {
         if (enemy.getName().equalsIgnoreCase(name)) {
            value = true;
            break;
         }
      }

      return value;
   }

   public static boolean isIgnore(String name) {
      for (Ignore ignore : getIgnores()) {
         if (ignore.getName().equalsIgnoreCase(name)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isOnIgnoreList(String name) {
      boolean value = false;

      for (Ignore ignore : getIgnores()) {
         if (ignore.getName().equalsIgnoreCase(name)) {
            value = true;
            break;
         }
      }

      return value;
   }

   public static Friend getFriend(String name) {
      for (Friend friend : getFriends()) {
         if (friend.getName().equalsIgnoreCase(name)) {
            return friend;
         }
      }

      return null;
   }

   public static Enemy getEnemy(String name) {
      for (Enemy enemy : getEnemies()) {
         if (enemy.getName().equalsIgnoreCase(name)) {
            return enemy;
         }
      }

      return null;
   }

   public static Ignore getIgnore(String name) {
      for (Ignore ignore : getIgnores()) {
         if (ignore.getName().equalsIgnoreCase(name)) {
            return ignore;
         }
      }

      return null;
   }

   public static void addFriend(String name) {
      if (!isOnFriendList(name)) {
         getFriends().add(new Friend(name));
      }
   }

   public static void delFriend(String name) {
      getFriends().remove(getFriend(name));
   }

   public static void addEnemy(String name) {
      if (!isOnEnemyList(name)) {
         getEnemies().add(new Enemy(name));
      }
   }

   public static void delEnemy(String name) {
      getEnemies().remove(getEnemy(name));
   }

   public static void addIgnore(String name) {
      if (!isOnIgnoreList(name)) {
         getIgnores().add(new Ignore(name));
      }
   }

   public static void delIgnore(String name) {
      getIgnores().remove(getIgnore(name));
   }

   public static void clearIgnoreList() {
      getIgnores().clear();
   }
}
