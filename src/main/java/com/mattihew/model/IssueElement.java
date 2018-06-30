package com.mattihew.model;

import com.mattihew.utils.NonNullObservableValue;
import com.mattihew.utils.TimerService;
import javafx.concurrent.ScheduledService;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class IssueElement
{
    private Hyperlink lblIssue;

    private StackPane stkIssue;

    private Label lblTime;

    private StackPane stkTime;

    private final TimeTracker timeTracker;

    private final String name;

    private final URI url;

    private final ScheduledService<String> service;

    public IssueElement(final String issue) throws IOException
    {
        this(issue, null);
    }

    public IssueElement(final String issue, final URI url) throws IOException
    {
        this.name = issue;
        this.url = url;

        this.lblIssue = new Hyperlink(name);
        this.lblIssue.addEventHandler(MouseEvent.MOUSE_CLICKED, this::clickLink);
        this.lblIssue.setFont(new Font(24));
        this.lblIssue.setAlignment(Pos.CENTER);

        this.stkIssue = new StackPane(this.lblIssue);
        this.stkIssue.addEventHandler(MouseEvent.MOUSE_ENTERED, this::hover);
        this.stkIssue.addEventHandler(MouseEvent.MOUSE_EXITED, this::hover);

        this.timeTracker = new TimeTracker();
        this.service = new TimerService(this.timeTracker);

        this.lblTime = new Label();
        this.lblTime.textProperty().bind(new NonNullObservableValue<>(service.lastValueProperty(),"0h 0m 0s"));
        this.lblTime.setFont(new Font(24));
        this.lblTime.setAlignment(Pos.CENTER);

        this.stkTime = new StackPane(this.lblTime);
        this.stkTime.addEventHandler(MouseEvent.MOUSE_ENTERED, this::hover);
        this.stkTime.addEventHandler(MouseEvent.MOUSE_EXITED, this::hover);
    }

    public Node getIssueNode()
    {
        return this.stkIssue;
    }

    public Node getTimeNode()
    {
        return this.stkTime;
    }

    public List<Node> getNodes()
    {
        return Arrays.asList(this.stkIssue, this.stkTime);
    }

    private void hover(final MouseEvent event)
    {
        for (final Node n : this.getNodes())
        {
            this.setStyleClass(n, "hover", MouseEvent.MOUSE_ENTERED.equals(event.getEventType()));
        }
    }

    private void setStyleClass(final Node node, final String styleClass, final boolean add)
    {
        if (add && !node.getStyleClass().contains(styleClass))
        {
            node.getStyleClass().add(styleClass);
        }
        else if (!add && node.getStyleClass().contains(styleClass))
        {
            node.getStyleClass().remove(styleClass);
        }
    }

    private void clickLink(final MouseEvent event)
    {
        if(this.url != null
                && !this.url.toString().isEmpty()
                && Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
        {
            try
            {
                Desktop.getDesktop().browse(this.url);
                event.consume();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void select()
    {
        this.getNodes().forEach(n -> this.setStyleClass(n, "active", true));
        this.timeTracker.startTimer();
        this.service.restart();
    }

    public void deselect()
    {
        this.getNodes().forEach(n -> this.setStyleClass(n, "active", false));
        this.timeTracker.stopTimer();
        this.service.cancel();
    }
}
