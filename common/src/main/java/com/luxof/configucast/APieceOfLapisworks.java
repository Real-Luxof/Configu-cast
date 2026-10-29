package com.luxof.configucast;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;

import java.util.ArrayList;
import java.util.List;

/** all of this was borrowed from Lapisworks */
public class APieceOfLapisworks {
	public static boolean matchShape(HexPattern pat1, HexPattern pat2) {
		// rat said that if you record how many times a position is drawn over then it's fine
		// they weren't too sure, but i pray they're right because nothing else i've done has worked
		List<HexCoord> pat2Positions = getPosesDrawnOver(pat2);
		for (HexDir dir : HexDir.values()) {
			if (equalsButUnordered(
				setTopLeftOrigin(getPosesDrawnOver(new HexPattern(dir, pat1.getAngles()))),
				setTopLeftOrigin(pat2Positions)
			)) return true;
		}
		return false;
	}

	public static List<HexCoord> getPosesDrawnOver(HexPattern pat) {
		HexCoord cursor = new HexCoord(0, 0);
		List<HexCoord> positions = new ArrayList<>();

		for (HexDir dir : pat.directions()) {
			positions.add(cursor);
			cursor = cursor.plus(dir);
			positions.add(cursor);
		}

		return positions;
	}

	public static List<HexCoord> setTopLeftOrigin(List<HexCoord> pat) {
		HexCoord runningTopLeft = new HexCoord(0, 0);
		for (HexCoord coord : pat) {

			if (coord.getQ() < runningTopLeft.getQ() && coord.getR() <= runningTopLeft.getR()) {
				runningTopLeft = new HexCoord(coord.getQ(), coord.getR());

			} else if (coord.getR() < runningTopLeft.getR()) {
				runningTopLeft = new HexCoord(coord.getQ(), coord.getR());

			}
		}
		// "must be effectively final" my fucking ass! fuck off!
		HexCoord topLeft = new HexCoord(runningTopLeft.getQ(), runningTopLeft.getR());

        return pat.stream().map((coord) -> {
            return new HexCoord(coord.getQ() - topLeft.getQ(), coord.getR() - topLeft.getR());
        }).toList();
	}

    /** Checks if two lists are equal, but does not check if their elements are ordered the same way. */
    public static <T extends Object> boolean equalsButUnordered(List<T> list1, List<T> list2) {
        if (list1.size() != list2.size()) { return false; }
        else if (list1.size() == 0) { return true; }

        List<T> workingOn = new ArrayList<>(list2);
        for (T thing : list1) {
            int idx = workingOn.indexOf(thing);
            if (idx == -1) { return false; }
            workingOn.remove(idx);
        }
        return true;
    }
}
