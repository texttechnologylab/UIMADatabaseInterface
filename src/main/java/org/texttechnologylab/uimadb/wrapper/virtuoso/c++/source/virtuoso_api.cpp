#include "virtuoso_api.hpp"
#include "VirtuosoConfig.hpp"
#include "sparqlib_light.hpp"



auto time_start() {
	std::wcout << L"**********************start************************" << std::endl;
	return std::chrono::high_resolution_clock::now();
}

auto time_end(std::chrono::time_point<std::chrono::steady_clock> &begin) {
	auto end = std::chrono::high_resolution_clock::now();
	std::wcout << L"**********************done************************" << std::endl;
	std::wcout << ((std::chrono::duration_cast<std::chrono::milliseconds>(end - begin).count()) / 1000) << L"s" << std::endl;
}


int main(int argc, char* argv[])
{
	virt db;
	utils util;

	if (argc < 3) { std::cout << L"not enough Parameters"; return -1; }

	std::wstring fnc = util.to_wstring(argv[1]);
	std::wstring dba = util.to_wstring(argv[2]);
	std::wstring uid = util.to_wstring(argv[3]);
	std::wstring pwd = util.to_wstring(argv[4]);
	std::wstring tkn = util.to_wstring(argv[5]);
	std::wstring id;
	if (argc > 6) {
		id = util.to_wstring(argv[6]);
	}

	Virtuoso v(dba, uid, pwd);

	if (fnc == L"c") {
		auto ff = v.CreateElement(tkn);
		std::wcout << L"ID: " << ff << std::endl;
	}
	else if (fnc == L"d") {
		auto begin = time_start();
		v.DeleteElement(tkn);
		time_end(begin);
	}
	else if (fnc == L"u") {
		auto  ff = v.UpdateElement(id, tkn);
		std::wcout << L"ID: " << ff << std::endl;
	}
	else if (fnc == L"g") {
		auto begin = time_start();
		v.GetElementOut(id, tkn);
		time_end(begin);
	}
	else if (fnc == L"f") {
		auto begin = time_start();
		std::map<std::wstring, std::wstring> map;
		v.fire_query(tkn, map);
		time_end(begin);
		for (auto& elem : map) {
			std::wcout << elem.first << L" | " << elem.second << std::endl;
		}
	}
	else {
		std::cout << "function db-name uid pwd [xmi-path, ID, out-path, query] (update/get)-id" << std::endl;
		std::cout << "c = Create Element" << std::endl;
		std::cout << "d = Delete Element" << std::endl;
		std::cout << "u = Update Element" << std::endl;
		std::cout << "g = Get Element" << std::endl;
		std::cout << "f = Fire Query" << std::endl;
	}
}
