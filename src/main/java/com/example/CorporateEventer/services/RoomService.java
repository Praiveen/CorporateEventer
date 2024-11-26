// package com.example.CorporateEventer.services;

// import com.example.CorporateEventer.entities.Room;
// import com.example.CorporateEventer.repositories.RoomRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Optional;

// @Service
// public class RoomService {

//     @Autowired
//     private RoomRepository roomRepository;

//     public List<Room> findAll() {
//         return roomRepository.findAll();
//     }

//     public Optional<Room> findById(Long id) {
//         return roomRepository.findById(id);
//     }

//     public Room save(Room room) {
//         return roomRepository.save(room);
//     }

//     public void deleteById(Long id) {
//         roomRepository.deleteById(id);
//     }
// } 