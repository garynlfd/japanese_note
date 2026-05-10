package com.japanesenote.model;

import java.util.List;

import com.japanesenote.model.Note;

public class PagedResponse {

    private List<Note> data;
    private long total;
    private int curPage;
    private int numOfPages;

    public PagedResponse(List<Note> data, long total, int curPage, int numOfPages) {
        this.data = data;
        this.total = total;
        this.curPage = curPage;
        this.numOfPages = numOfPages;
    }

    // ── Getters Only ──────────────────────────────────────────────────────

    public List<Note> getData() { return data; }

    public long getTotal() { return total; }

    public int getCurPage() { return curPage; }
    
    public int getNumOfPages() { return numOfPages; }
}
