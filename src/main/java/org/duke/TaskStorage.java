package org.duke;

import org.duke.json.JsonParser;
import org.duke.json.JsonWriter;
import org.duke.json.ValueHandler;
import org.duke.task.Task;
import org.duke.task.TaskType;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

public class TaskStorage {
    private static final String SAVE_PATH = "./duke.json";
    private final ArrayList<Task> taskList;

    public TaskStorage(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    public static TaskStorage load() {
        try (FileReader read = new FileReader(SAVE_PATH)) {
            return new TaskStorage(JsonParser.parse(read, ValueHandler.listOf(new TaskType.Builder())));
        } catch (FileNotFoundException e) {
            return new TaskStorage(new ArrayList<>());
        } catch (IOException e) {
            throw new DukeException("Unable to load saved data", e);
        }
    }

    public void save() {
        try (FileWriter write = new FileWriter(SAVE_PATH);
             JsonWriter jw = new JsonWriter(write)) {
            jw.writeValue(taskList);
        } catch (Exception e) {
            throw new DukeException(e);
        }
    }

    public void add(Task t) {
        this.taskList.add(t);
    }


    public int size() {
        return this.taskList.size();
    }

    public Task remove(int index) {
        return this.taskList.remove(index);
    }

    public Stream<Task> stream() {
        return this.taskList.stream();
    }

    public Task get(int i) {
        return this.taskList.get(i);
    }
}