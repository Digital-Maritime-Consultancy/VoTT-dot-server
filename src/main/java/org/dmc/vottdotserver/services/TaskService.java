package org.dmc.vottdotserver.services;

import lombok.extern.slf4j.Slf4j;
import org.dmc.vottdotserver.exceptions.DataNotFoundException;
import org.dmc.vottdotserver.models.domain.Task;
import org.dmc.vottdotserver.models.domain.enums.AssetState;
import org.dmc.vottdotserver.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.TimeZone;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class TaskService {
    @Autowired
    TaskRepository taskRepository;

    /**
     * Get all the tasks.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        log.debug("Request to get all Tasks");
        return this.taskRepository.findAll();
    }

    /**
     * Get one task by ID.
     *
     * @param uuid        the ID of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Task findOne(UUID uuid) throws DataNotFoundException {
        log.debug("Request to get Task : {}", uuid);
        return taskRepository.findById(uuid).stream().findFirst()
                .orElseThrow(() -> new DataNotFoundException("No task found for the provided ID", null));
    }

    /**
     * Save a task.
     *
     * @param task  the entity to save
     * @return the persisted entity
     */
    @Transactional
    public Task save(Task task) throws DataNotFoundException, MissingFormatArgumentException {
        log.debug("Request to save Task : {}", task);

        this.taskRepository.findById(task.getId()).ifPresentOrElse(taskInDB -> {
            task.update(taskInDB);
            task.setLastUpdatedAt(this.getCurrentUTCTimeISO8601());
            if (task.getLastUsedForProjectCreation() == null){
                task.setLastUsedForProjectCreation("");
            }
        }, () -> {
            task.setCreatedAt(this.getCurrentUTCTimeISO8601());
            task.setLastUpdatedAt(task.getCreatedAt());
            task.setLastUsedForProjectCreation("");
        });

        if (task.getAttributeKeys() == null) {
            task.setAttributeKeys(new HashMap<>());
        }

        if(task.getProgress()==null) {
            if(task.getImageList() == null || task.getImageList().isEmpty()){
                task.setProgress(new HashMap<>());
            }
            else{
                Map<String, AssetState> progress = new HashMap<>();
                Iterator<Map.Entry<String, String>> iterator = task.getImageList().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    progress.put(entry.getKey(), AssetState.NOTVISITED);
                }
                task.setProgress(progress);
            }
        }

        this.validateTaskForSave(task);

        return this.taskRepository.save(task);
    }

    /**
     * Delete the task by ID.
     *
     * @param uuid        the ID of the entity
     */
    @Transactional(propagation = Propagation.NESTED)
    public void delete(UUID uuid) throws DataNotFoundException {
        log.debug("Request to delete Task : {}", uuid);
        this.taskRepository.findById(uuid)
                .ifPresentOrElse(i -> {
                    this.taskRepository.deleteById(i.getId());
                }, () -> {
                    throw new DataNotFoundException("No task found for the provided ID", null);
                });
    }

    public String getCurrentUTCTimeISO8601() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    private void validateTaskForSave(Task task) {
        if(task == null) {
            return;
        }

        if(task.getId() == null){
            throw new MissingFormatArgumentException("No ID found");
        }
        if(task.getStellaUrl() == null){
            throw new MissingFormatArgumentException("No Stella URL found");
        }
        if(task.getVottBackendUrl() == null){
            throw new MissingFormatArgumentException("No VoTT backend url found");
        }
        if(task.getImageServerUrl() == null){
            throw new MissingFormatArgumentException("No image server url found");
        }
        if(task.getTaskServerUrl() == null){
            throw new MissingFormatArgumentException("No task server url found");
        }
    }

    public boolean doesExist(UUID uuid) {
        return taskRepository.findById(uuid).isPresent();
    }
}
