#include "Virtuoso.hpp"

Virtuoso::Virtuoso(const std::wstring& config_file) {

    try {
        VirtuosoConfig config(config_file);
        dsn = config.get_database_name();
        uid = config.get_username();
        pwd = config.get_password();
        auto err = (connector.ODBC_Connect(dsn.c_str(), uid.c_str(), pwd.c_str())) ? -1 : connector.ODBC_Disconnect();
        if (err == -1) {
            std::wcout << L"No Database Connection" << std::endl;
        }
        connector.ODBC_Disconnect();
    }
    catch (std::exception & e) {
        std::wcout << e.what() << std::endl;
    }

}

Virtuoso::Virtuoso(const std::wstring& database_name, const std::wstring& user_id, const std::wstring& password): dsn(database_name), uid(user_id), pwd(password) {
    try {
        connector.dsn = dsn;
        connector.uid = uid;
        connector.pwd = pwd;
        auto err = (connector.ODBC_Connect(dsn.c_str(), uid.c_str(), pwd.c_str())) ? -1 : connector.ODBC_Disconnect();
        if (err == -1) {
            std::wcout << L"No Database Connection" << std::endl;
        }
        connector.ODBC_Disconnect();
    }
    catch (std::exception & e) {
        std::wcout << e.what() << std::endl;
    }
}

/**
/* Establishes a connection to the Database
*/
int Virtuoso::connect() {
    try {
        auto err = (connector.ODBC_Connect(dsn.c_str(), uid.c_str(), pwd.c_str())) ? -1 : connector.ODBC_Disconnect();
        if (err != 0) {
            std::wcout << L"No Database Connection" << std::endl;
        }
        return err;
    }
    catch (std::exception & e) {
        std::wcout << e.what() << std::endl;
        return -1;
    }
}

/**
/* Dissconects the object from the Database
*/
int Virtuoso::disconnect() {
    try {
        auto err = connector.ODBC_Disconnect();
        if (err != 0) {
            std::wcout << L"Could not Disconnect from DB" << std::endl;
        }
        return err;
    }
    catch (std::exception & e) {
        std::wcout << e.what() << std::endl;
        return -1;
    }
}

/**
/* Updates a Graph inside the Database. It removes the old graph and creates a new one
/* @param id: is the ID of the graph that shall be updated
/* @param xmi_file: path incl. filename of the xmi-file
*/
std::wstring Virtuoso::UpdateElement(std::wstring& id, std::wstring &xmi_file) {

    auto err = DeleteElement(id);
    if (err != 1) { return L""; }
    return CreateElement(xmi_file);
    
}

/**
/* Removes a Graph from the Databse
/* @param id: is the ID of the graph that shall be deleted
*/
int Virtuoso::DeleteElement(std::wstring& id) {
    auto cmd_clr = fmt::format(L"SPARQL CLEAR GRAPH <{0}>", id);
    auto cmd_del = fmt::format(L"SPARQL DROP GRAPH <{0}>", id);
    auto err = connector.ODBC_Connect();
    if (err != 0) { return err; }
    err = connector.ODBC_Execute(cmd_clr.c_str());
    err = connector.ODBC_Execute(cmd_del.c_str());
    if (err != 0) { return err; }
    connector.ODBC_Disconnect();
    return err;
}

/**
/* Reads an Xmi-File and stores it inside a Virtuoso Database
/* @param xmi_file: path incl. filename of the xmi-file
*/
std::wstring Virtuoso::CreateElement(std::wstring &xmi_file) {
    std::wstring guid;
    util.create_number_guid(guid);

    auto s_xmi_file = util.to_string(xmi_file);
    file<wchar_t> xmlFile(s_xmi_file.data());
    xml_document<wchar_t> doc;
    doc.parse<0>(xmlFile.data());


    auto id = v_helper.get_id_from_xmi(doc);
    auto graph_id = (id != L"") ? id : guid;
    v_helper.set_graph_id(graph_id);
    
    std::wcout << L"**********************start************************" << std::endl;
    auto begin = std::chrono::high_resolution_clock::now();

    std::thread thr_read([&](){v_helper.xmi_to_queue(doc); });
    std::thread thr_write([&](){v_helper.queue_to_db(connector); });

    thr_read.join();
    thr_write.join();

    auto end = std::chrono::high_resolution_clock::now();
    std::wcout << L"**********************done************************" << std::endl;
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - begin).count();
    std::wcout << (duration / 1000) << L"s" << std::endl;


    return graph_id;
}

/**
/* Fetches a Graph from the Database and stores it inside a file on the drive
/* @param id: is the ID of the graph that shall be fetched
/* @param out_file: the file where the document shall be stored
*/
int Virtuoso::GetElement(std::wstring &id) {
    try {
        v_helper.set_graph_id(id);
        v_helper.get_file(connector);
        return 1;
    }
    catch (std::exception & e) {
        throw e;
    }
}

/**
/* Fetches a Graph from the Database and stores it inside a file on the drive
/* @param id: is the ID of the graph that shall be fetched
/* @param out_file: the file where the document shall be stored
*/
int Virtuoso::GetElementOut(std::wstring& id, std::wstring& out_file) {
    try {
        v_helper.set_graph_id(id);
        v_helper.write_to_file(out_file, connector);
        return 1;
    }
    catch (std::exception & e) {
        throw e;
    }
}


/**
/* Fires a SPARQL-Query
/* @param query: is the query that shall be fired
/* @param result: is a 2d map that stores all asked information
*/
int Virtuoso::fire_query(std::wstring &query, std::map<std::wstring, std::wstring> &result) {
    try {
        connector.ODBC_Connect();
        auto err = connector.ODBC_Execute(query.c_str());
        if (err != 0) { return -1; }
        err = connector.ODBC_ResultToString(result);
        if (err != 0) { return -1; }
        connector.ODBC_Disconnect();
        return 0;
    }
    catch (std::exception & e) {
        throw e;
    }

}

