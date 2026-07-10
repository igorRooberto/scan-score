package com.project.ScanScoreJava.core.services;

import com.project.ScanScoreJava.infrastructure.web.dto.DetectedBubbleDTO;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class BubbleGridOrganizer {

    // Vertical tolerance (in pixels) to group bubbles into the same row (question).
    // This accounts for slight tilts or misalignments in the scanned image.
    private final int ROW_Y_TOLERANCE = 15;

    public Map<Integer, List<DetectedBubbleDTO>> organize(List<DetectedBubbleDTO> rawBubbles) {

        if (rawBubbles == null || rawBubbles.isEmpty()) {
            throw new IllegalArgumentException("Raw bubbles list cannot be null or empty.");
        }

        rawBubbles.sort(Comparator.comparingInt(DetectedBubbleDTO::y));

        Map<Integer, List<DetectedBubbleDTO>> grid = new TreeMap<>();
        List<DetectedBubbleDTO> currentRow = new ArrayList<>();
        int questionNumber = 1;

        currentRow.add(rawBubbles.getFirst());

        for (int i = 1; i < rawBubbles.size(); i++) {
            DetectedBubbleDTO bubble = rawBubbles.get(i);
            DetectedBubbleDTO previousBubble = rawBubbles.get(i - 1);

            int difference = Math.abs(bubble.y() - previousBubble.y());

            if(difference > ROW_Y_TOLERANCE){

                if (currentRow.size() >= 4) {
                    saveRowToGrid(grid, currentRow, questionNumber);
                    questionNumber++;
                }
                currentRow = new ArrayList<>();
            }
            currentRow.add(bubble);
        }

        // Saves the last row (question) that remained pending after the loop and hasn't been saved to the grid
        if (currentRow.size() >= 4) {
            saveRowToGrid(grid, currentRow, questionNumber);
        }

        return grid;
    }

    private void saveRowToGrid(Map<Integer, List<DetectedBubbleDTO>> grid, List<DetectedBubbleDTO> row, int questionNumber){
        row.sort(Comparator.comparingInt(DetectedBubbleDTO::x));
        grid.put(questionNumber,row);
    }

}

