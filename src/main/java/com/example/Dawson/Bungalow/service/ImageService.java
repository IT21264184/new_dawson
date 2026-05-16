package com.example.Dawson.Bungalow.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFsOperations gridFsOperations;

    /**
     * Upload a single image and return its GridFS ID string.
     */
    public String uploadImage(MultipartFile file) throws IOException {
        DBObject metadata = new BasicDBObject();
        metadata.put("contentType", file.getContentType());
        metadata.put("fileSize", file.getSize());

        ObjectId id = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                metadata
        );
        return id.toHexString();
    }

    /**
     * Upload multiple images and return a list of GridFS ID strings.
     */
    public List<String> uploadImages(List<MultipartFile> files) throws IOException {
        List<String> ids = new ArrayList<>();
        for (MultipartFile file : files) {
            ids.add(uploadImage(file));
        }
        return ids;
    }

    /**
     * Retrieve raw image bytes by GridFS ID.
     */
    public byte[] getImageById(String id) throws IOException {
        GridFSFile file = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(id)))
        );
        if (file == null) {
            throw new RuntimeException("Image not found: " + id);
        }
        GridFsResource resource = gridFsOperations.getResource(file);
        return resource.getInputStream().readAllBytes();
    }

    /**
     * Get content type of an image by GridFS ID.
     */
    public String getContentType(String id) {
        GridFSFile file = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(id)))
        );
        if (file == null) return "image/jpeg"; // fallback
        return file.getMetadata() != null
                ? file.getMetadata().getString("contentType")
                : "image/jpeg";
    }

    /**
     * Delete a single image by GridFS ID.
     */
    public void deleteImage(String id) {
        gridFsTemplate.delete(
                new Query(Criteria.where("_id").is(new ObjectId(id)))
        );
    }

    /**
     * Delete multiple images by their GridFS IDs.
     */
    public void deleteImages(List<String> ids) {
        if (ids == null) return;
        for (String id : ids) {
            deleteImage(id);
        }
    }
}