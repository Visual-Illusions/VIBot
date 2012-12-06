package net.visualillusionsent.vibot;

/**
 * @author Jason (darkdiplomat)
 */
public final class Topic {
    private String topic, setBy, date;

    public Topic(String topic) {
        this.topic = topic;
    }

    /**
     * Gets the topic text
     * 
     * @return the topic text
     */
    public final String getTopic() {
        return topic;
    }

    final void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Gets the nick that set the topic
     * 
     * @return the nick that set the topic
     */
    public final String getSetBy() {
        return setBy;
    }

    final void setSetBy(String setBy) {
        this.setBy = setBy;
    }

    /**
     * Gets the Date of when the topic was set
     * 
     * @return date of when the topic was set
     */
    public final String getDate() {
        return date;
    }

    final void setDate(String date) {
        this.date = date;
    }

    public final String toString() {
        return String.format("Topic[Topic=%s SetBy=%s Date=%s]", topic, setBy, date);
    }

}
