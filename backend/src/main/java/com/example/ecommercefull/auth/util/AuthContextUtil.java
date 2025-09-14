package com.example.ecommercefull.auth.util;

import com.example.ecommercefull.auth.models.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthContextUtil {

    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof User){
            return (User) authentication.getPrincipal();
        }
        return null;
    }
    public Long getCurrentUserId(){
        User currentUser = getCurrentUser();
        return currentUser!=null ? currentUser.getId() : null;
    }

    public String getCurrentUsername(){
        User currentUser = getCurrentUser();
        return currentUser!=null ? currentUser.getUsername() : null;
    }

    public String getCurrentRole(){
        User currentUser = getCurrentUser();
        return currentUser!=null ? currentUser.getRole().name() : null;
    }
}
