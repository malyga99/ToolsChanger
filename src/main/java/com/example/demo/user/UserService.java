package com.example.demo.user;

public interface UserService {

    User getCurrentUser();

    User findById(Long id);

}
