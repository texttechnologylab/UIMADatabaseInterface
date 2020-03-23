/**
* This Class is a utility class for different purposes.
* It handles filereading and different StringOperations
*
* @author Khun
* @version 1.0
*/

#include "utils.hpp"

// converts wstring to string(if possible the string is correct.
// With extended unicodecaracters there might appear garbage)
std::string utils::to_string(std::wstring wstr) {
	return strconverter.to_bytes(wstr);
}

// converts string to wstring
std::wstring utils::to_wstring(std::string str) {
	return strconverter.from_bytes(str);
}

// extract the last element of a Filepath
std::wstring utils::extract_filename(std::wstring& filename) {
	auto pos = filename.find_last_of(LR"(\)");
	return filename.substr(pos + 1, filename.length());
}

// Splits a String at a given delimiter once
std::pair<std::wstring, std::wstring> utils::split_at_delimiter(std::wstring& str, const std::wstring& delimiter) {
	auto pos = str.find(delimiter, 0);
	auto p1 = str.substr(0, pos);
	auto p2 = str.substr(pos + 1, str.length());
	trim(p1);
	trim(p2);
	return std::make_pair(p1, p2);
}

// Writes file from disc to buffer (UNICODE)
std::wstring utils::file_to_string(const std::wstring& filepath) {
	std::wifstream sstrm(filepath);
	std::wstringstream buffer;
	buffer << sstrm.rdbuf();
	return buffer.str();
}

// Writes file from disc to buffer (ASCII)
std::string utils::file_to_string(const std::string& filepath) {
	std::ifstream sstrm(filepath);
	std::stringstream buffer;
	buffer << sstrm.rdbuf();
	return buffer.str();
}

// Checks if given string is a number or not
bool utils::is_number(const std::string& s) {
	std::string::const_iterator it = s.begin();
	while (it != s.end() && std::isdigit(*it)) ++it;
	return !s.empty() && it == s.end();
}

// Checks if given string is a number or not
bool utils::is_number(const std::wstring& s) {
	std::wstring::const_iterator it = s.begin();
	while (it != s.end() && std::isdigit(*it)) ++it;
	return !s.empty() && it == s.end();
}


std::string utils::get_time_as_string() {
	auto p = std::chrono::system_clock::now();
	std::time_t t = std::chrono::system_clock::to_time_t(p);
	return std::ctime(&t);
}

std::wstring utils::get_time_as_wstring() {
	return to_wstring(get_time_as_string());
}



/**
* Creates GUID(unique ID) and Converts it to a wstring
* @Param: id: the new greated GUID is stored in this parameter
*/
int utils::create_guid(std::wstring& guid) {
	try {
		GUID gidReference;
		OLECHAR* guidString;
		return (CoCreateGuid(&gidReference) == S_OK) ? (StringFromCLSID(gidReference, &guidString) == S_OK) ? (guid = guidString, 1) : -1 : -1;
	}
	catch (std::exception) {
		return -1;
	}
}


/**
* Creates GUID(unique ID) and Converts it to a wstring
* @Param: id: the new greated GUID is stored in this parameter
*/
int utils::create_number_guid(std::wstring& guid) {

	auto err = create_guid(guid);
	if (err == -1) { return -1; }
	char chars[] = "{}- ";

	for (unsigned int i = 0; i < strlen(chars); ++i)
	{
		// you need include <algorithm> to use general algorithms like std::remove()
		guid.erase(std::remove(guid.begin(), guid.end(), chars[i]), guid.end());
	}

	return err;
}


std::wstring utils::escape_braces(std::wstring str) {
	
	auto repl = [&str](wchar_t* token, wchar_t* repl_token) {
		auto pos = str.find(token, 0);
		while (pos != str.npos) {
			str.replace(pos, 1, repl_token);
			pos = str.find(token, pos + 2);
		}
	};

	repl(L"{", L"{{");
	repl(L"}", L"}}");

	return str;
}