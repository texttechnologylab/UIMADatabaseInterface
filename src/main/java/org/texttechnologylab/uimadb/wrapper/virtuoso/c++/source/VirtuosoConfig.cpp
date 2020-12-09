/**
* This Class handles the reagin of a configfile for a Virtuoso-Database
*
* @author Khun
* @version 1.0
*/

#include "VirtuosoConfig.hpp"



VirtuosoConfig::VirtuosoConfig(const std::wstring& path_file) {
	utils util;	
	std::wifstream file;
	file.open(path_file);
	std::wstring line;
	while (std::getline(file, line)) {
        std::wistringstream is_line(line);
        std::wstring key;
        if (std::getline(is_line, key, L'='))
        {
            std::wstring value;
            if (std::getline(is_line, value))
                util.trim(key);
                util.trim(value);
                config_data[key] = value;
        }
	}
}
const std::wstring VirtuosoConfig::get_host() { return config_data[L"virtuoso_host"]; }
const std::wstring VirtuosoConfig::get_database_name() { return config_data[L"virtuoso_db"]; }
const std::wstring VirtuosoConfig::get_collection() { return config_data[L"virtuoso_collectionname"]; }
const std::wstring VirtuosoConfig::get_username() { return config_data[L"virtuoso_username"]; }
const std::wstring VirtuosoConfig::get_password() { return config_data[L"virtuoso_password"]; }