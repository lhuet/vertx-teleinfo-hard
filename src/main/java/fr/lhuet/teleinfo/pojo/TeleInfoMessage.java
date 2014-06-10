package fr.lhuet.teleinfo.pojo;

import java.util.Date;

/**
 * Created by lhuet on 30/05/14.
 */
public class TeleInfoMessage {

    private Date date;
    private int indexcpt;
    private float pmoy;
    private float imoy;
    private int pmax;
    private int imax;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getIndexcpt() {
        return indexcpt;
    }

    public void setIndexcpt(int indexcpt) {
        this.indexcpt = indexcpt;
    }

    public float getPmoy() {
        return pmoy;
    }

    public void setPmoy(float pmoy) {
        this.pmoy = pmoy;
    }

    public float getImoy() {
        return imoy;
    }

    public void setImoy(float imoy) {
        this.imoy = imoy;
    }

    public int getPmax() {
        return pmax;
    }

    public void setPmax(int pmax) {
        this.pmax = pmax;
    }

    public int getImax() {
        return imax;
    }

    public void setImax(int imax) {
        this.imax = imax;
    }
}
