package seedu.progresschecker.logic.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.api.services.tasks.model.Task;

import seedu.progresschecker.commons.core.EventsCenter;
import seedu.progresschecker.commons.events.ui.LoadTaskEvent;
import seedu.progresschecker.commons.util.FileUtil;
import seedu.progresschecker.logic.commands.exceptions.CommandException;
import seedu.progresschecker.model.task.MyTaskList;

/**
 * View the web view of a particular TaskList (with the name provided).
 */
public class ViewTaskListCommand extends Command {

    public static final String COMMAND_WORD = "viewtask";
    public static final String COMMAND_ALIAS = "vt";
    public static final String TASK_PAGE = "tasklist.html";
    public static final String DEFAULT_TASK_PAGE = "/nulltasklist.html";
    public static final String FILE_FAILURE = "Something wrong with the file system.";
    public static final String COMMAND_FORMAT = COMMAND_WORD + "TASKLIST-TITLE";
    public static final String MESSAGE_TITLE_CONSTRAINTS = "The title of a task list should not exceed "
            + "49 characters (as specified by Google Task.";
    public static final int MAX_TITLE_LENGTH = 49;

    public static final String MESSAGE_USAGE = COMMAND_WORD
            // TODO: change description and parameter range when appropriate
            + ": Toggle view to display the task list with the given name.\n"
            + "Parameters: TASKLIST-TITLE (max "
            + MAX_TITLE_LENGTH
            + " characters)\n"
            + "Example: " + COMMAND_WORD + " CS2103 LOs";

    public static final String MESSAGE_SUCCESS = "Viewing task list: %1$s";

    private final String listName;

    public ViewTaskListCommand(String name) {
        this.listName = name;
    }

    @Override
    public CommandResult execute() throws CommandException {
        List<Task> list = MyTaskList.searchTaskList(listName);
        File htmlFile = new File("data/" + TASK_PAGE);
        writeToHtml(list, htmlFile);
        try {
            EventsCenter.getInstance().post(new LoadTaskEvent(readFile(htmlFile.getAbsolutePath(),
                    StandardCharsets.UTF_8)));
        } catch (IOException ioe) {
            throw new CommandException(FILE_FAILURE);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, listName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ViewTaskListCommand // instanceof handles nulls
                && this.listName.equals(((ViewTaskListCommand) other).listName)); // state check
    }

    /**
     * Writes the loaded task list to an html file.Loads the tasks.
     *
     * @param list task list serialized in a java List.
     * @param file File object of the html file.
     */
    void writeToHtml(List<Task> list, File file) throws CommandException {
        int size = list.size();

        try {
            FileUtil.createIfMissing(file);

            FileWriter fw1 = new FileWriter(file, false);
            BufferedWriter bw1 = new BufferedWriter(fw1);
            PrintWriter out1 = new PrintWriter(bw1);

            out1.print("");

            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            out.print("<!DOCTYPE html>\n" + "<html>\n" + "<body>\n");
            out.print("<h1>" + listName + "</h1>\n" + "<dl>\n");

            for (int i = 0; i < (size - 2); i++) {
                Task task = list.get(i);
                out.print("    <dt>" + task.getTitle() + "</dt>\n");
                out.print("    <dd>Deadline: " + task.getDue() + "</dd>\n");
                out.print("    <dd>Status: " + task.getStatus() + "</dd>\n");
                out.print("    <dd>PS: " + task.getNotes() + "</dd>\n");

            }

            out.print("</dl>\n" + "</body>\n" + "</html>\n");

            out.close();

        } catch (IOException e) {
            throw new CommandException(FILE_FAILURE);

        }
    }

    /**
     * Reads the content of a text file to a String.
     *
     * @param path file path
     * @param encoding the encoding standard, such as StandardCharsets.UTF_8.
     */
    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * Reads the relative path to the resource directory from the <code>RESOURCE_PATH</code> file located in
     * <code>src/main/resources</code>
     * @return the relative path to the <code>resources</code> in the file system, or
     *         <code>null</code> if there was an error
     */
    private static String getResourcePath(String resourcePaths) {
        try {
            URI resourcePathFile = System.class.getResource(resourcePaths).toURI();
            String resourcePath = Files.readAllLines(Paths.get(resourcePathFile)).get(0);
            URI rootUri = new File("").toURI();
            URI resourceUri = new File(resourcePath).toURI();
            URI relativeResourceUri = rootUri.relativize(resourceUri);
            return relativeResourceUri.getPath();
        } catch (Exception e) {
            return null;
        }
    }
}