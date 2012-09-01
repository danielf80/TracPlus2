package com.redxiii.tracplus.ejb.entity;

import java.io.Serializable;



public class User implements Serializable {
	
   private static final long serialVersionUID = 1L;
   
   private String username;
   private String name;

   public String getUsername() {
      return username;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getName() {
      return name;
   }

   @Override
   public String toString() {
      return "User (username = " + username + ", name = " + name + ")";
   }

}
