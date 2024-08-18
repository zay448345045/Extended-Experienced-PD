/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2020 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.messages;

import java.util.Locale;

public enum Languages {
	ENGLISH("english",      "",   Status._COMPLETE_,   null, null);
        CHINESE("中文",         "zh", Status.UNFINISHED, new String[]{"Chronie_Lynn_Iwa", "Jinkeloid(zdx00793)", "endlesssolitude", "catand"}, new String[]{"931451545", "Budding", "Fatir", "Fishbone", "Hcat", "HoofBumpBlurryface", "Horr_lski", "Lery", "Lyn_0401", "Lyx0527", "Ooooscar", "RainSlide", "ShatteredFlameBlast", "SpaceAnchor", "Teller", "hmdzl001", "leo", "tempest102", "户方狸奴", "catand", "zqiao"});
	GREEK("ελληνικά",       "el", Status.UNFINISHED, new String[]{"Aeonius", "Saxy"}, new String[]{"DU_Clouds", "VasKyr", "YiorgosH", "fr3sh", "stefboi", "toumbo", "val.exe"}),
	DUTCH("nederlands",     "nl", Status.UNREVIEWED, new String[]{"AlbertBrand", "Mvharen"}, new String[]{"AvanLieshout", "Blokheck011", "Frankwert", "Gehenna", "Valco", "ZephyrZodiac", "link200023", "rmw", "th3f4llenh0rr0r"}),
	UKRANIAN("українська",  "uk", Status.UNREVIEWED, new String[]{"Oster", "Snikewin", "zhushman00"}, new String[]{"AlexFenixUA", "Dotsent", "Lyttym", "Mops", "Sadsaltan1", "TarasUA", "TheGuyBill", "Tomfire", "Volkov", "ZverWolf", "_bor_", "alexfenixva", "ddmaster3463", "filalex77", "holuydadko", "ingvarfed", "iu0v1", "lezzen", "myshokoleksander05", "oliolioxinfree", "qweez", "romanokurg", "so1der", "sterenkevicsasa", "vlisivka", "xojltoh", "yukete", "zhawty", "Мальвочка"}),
	VIETNAMESE("tiếng việt","vi", Status.UNREVIEWED, new String[]{"Chuseko", "The_Hood", "nguyenanhkhoapythus"}, new String[]{"BlueSheepAlgodoo", "Phuc2401", "Teh_boi", "Threyja", "Toluu", "bruhwut", "buicongminh_t63", "deadlevel13", "h4ndy_c4ndy", "hniV", "khangxyz3g", "ngolamaz3", "nkhhu", "vdgiapp", "vtvinh24"}),
	
//	BELARUSIAN("беларуская","be", Status.UNREVIEWED, new String[]{"AprilRain(Vadzim Navumaû)"}, new String[]{"4ebotar"});
	
	public enum Status{
		//below 80% translated languages are not added or removed
		UNFINISHED, //80-99% translated
		UNREVIEWED, //100% translated
		_COMPLETE_  //100% reviewed
	}

	private String name;
	private String code;
	private Status status;
	private String[] reviewers;
	private String[] translators;

	Languages(String name, String code, Status status, String[] reviewers, String[] translators){
		this.name = name;
		this.code = code;
		this.status = status;
		this.reviewers = reviewers;
		this.translators = translators;
	}

	public String nativeName(){
		return name;
	}

	public String code(){
		return code;
	}

	public Status status(){
		return status;
	}

	public String[] reviewers() {
		if (reviewers == null) return new String[]{};
		else return reviewers.clone();
	}

	public String[] translators() {
		if (translators == null) return new String[]{};
		else return translators.clone();
	}

	public static Languages matchLocale(Locale locale){
		return matchCode(locale.getLanguage());
	}

	public static Languages matchCode(String code){
		for (Languages lang : Languages.values()){
			if (lang.code().equals(code))
				return lang;
		}
		return ENGLISH;
	}

}
