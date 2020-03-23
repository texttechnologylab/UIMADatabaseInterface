/**
* This Class is a utility class for different purposes.
* It handles filereading and different StringOperations
*
* @author Khun
* @version 1.0
*/

#define _SILENCE_CXX17_CODECVT_HEADER_DEPRECATION_WARNING
#define _SILENCE_ALL_CXX17_DEPRECATION_WARNINGS

#ifndef Utils_H
#define Utils_H


#pragma once
#include "pch.h"

class utils {
private:
	using convert_t = std::codecvt_utf8<wchar_t>;
	std::wstring_convert<convert_t, wchar_t> strconverter;

public:
	std::string to_string(std::wstring wstr);
	std::wstring to_wstring(std::string str);
	std::wstring extract_filename(std::wstring& filename);
	std::pair<std::wstring, std::wstring> split_at_delimiter(std::wstring& str, const std::wstring& delimiter);
	std::wstring file_to_string(const std::wstring& filepath);
	std::string file_to_string(const std::string& filepath);
	bool is_number(const std::string& s);
	bool is_number(const std::wstring& s);
	int create_guid(std::wstring& guid);
	int create_number_guid(std::wstring& guid);
	std::string utils::get_time_as_string();
	std::wstring get_time_as_wstring();
	std::wstring escape_braces(std::wstring str);

	/********************************************** INLINE FUNCTIONS **********************************************/

	// trim from the beginning
	static inline void utils::ltrim(std::wstring& s) {
		s.erase(s.begin(), std::find_if(s.begin(), s.end(), [](int ch) {
			return !std::isspace(ch);
			}));
	}

	// trim from the end
	static inline void utils::rtrim(std::wstring& s) {
		s.erase(std::find_if(s.rbegin(), s.rend(), [](int ch) {
			return !std::isspace(ch);
			}).base(), s.end());
	}

	// trim from both ends
	static inline void utils::trim(std::wstring& s) {
		ltrim(s);
		rtrim(s);
	}
};

#endif