/**
* This Class handles the reagin of a configfile for a Virtuoso-Database
*
* @author Khun
* @version 1.0
*/

#ifndef VirtuosoConfig_H
#define VirtuosoConfig_H

#include "utils.hpp"
#include <string>
#include <iostream>
#include <fstream>



class VirtuosoConfig {
private:
	//Classvariables
	std::wstring v_host;
	std::wstring v_database_name;
	std::wstring v_collection;
	std::wstring v_username;
	std::wstring v_password;
	std::unordered_map<std::wstring, std::wstring> config_data;

public:
	const VirtuosoConfig(const std::wstring & path_file);
	const std::wstring get_host();
	const std::wstring get_database_name();
	const std::wstring get_collection();
	const std::wstring get_username();
	const std::wstring get_password();
};

#endif