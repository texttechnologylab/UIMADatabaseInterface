#ifndef SPARQLIB_LIGHT_HPP
#define SPARQLIB_LIGHT_HPP

#include <string>
#include <stack>
#include <iostream>


class Sparkel
{
private:
    std::wostream& os;
    bool tag_open;
    bool newline;
    std::stack<std::wstring> elt_stack;
    const std::wstring Newline = L"\n";
    const std::wstring Sparql  = L"SPARQL ";
    const std::wstring Space  = L"  ";
    const std::wstring Insert = L"INSERT IN GRAPH <";

    inline void open_tag() {
        this->os << L"<";
    }

    inline void close_tag() {
        this->os << L"> ";
    }

    inline void quote_tag() {
        this->os << LR"(")";
    }

    inline void close_triplet() {
        os << L" .";
    }

    inline void end_sparkel() {
        if (tag_open) {
            {
                this->os << L" }";
                tag_open = false;
            }
        }
    }

    inline void space() {
        os << (Space);
    }

    inline void write_subs(const wchar_t* str) {
        for (; *str; str++) {
            switch (*str) {
            case L'&': os << L"&amp;"; break;
            case L'<': os << L"&lt;"; break;
            case L'>': os << L"&gt;"; break;
            case L'\'': os << L"&apos;"; break;
            case L'"': os << L"&quot;"; break;
            //case L'\u0084': os << L"&guot;"; break;
            default: os.put(*str); break;
            }
        }
    }

    inline void write_token(const wchar_t* str) {
        open_tag();
        write_subs(str);
        close_tag();
    }

    inline void write_object_token(const wchar_t* str) {
        quote_tag();
        write_subs(str);
        quote_tag();
    }

public:

    Sparkel(std::wostream& os) : os(os), tag_open(false), newline(true) { os; }
    ~Sparkel() {}

    Sparkel& open_query() {
        this->os << Sparql;
        tag_open = true;
        return *this;
    }


    Sparkel& open_insert(const wchar_t *graphname) {
        open_query();
        this->os << Insert << graphname << L"> {";
        newline = true;
        return *this;
    }

    Sparkel& close_query() {
	    this->end_sparkel();
	    return *this;
    }

    Sparkel& add_triplet(const wchar_t *subject, const wchar_t* predicat, const wchar_t* object) {
        if (newline) { os << Newline; }
        write_token(subject);
        space();
        write_token(predicat);
        space();
        write_object_token(object);
        close_triplet();
        newline = true;
        return *this;
    }

    Sparkel& add_bin(const wchar_t *file_name) {
        open_tag();
        this->os << file_name;
        close_tag();
        return *this;
    }

    //inserts a string. watch out: it inserts everything. There is no prove that the query will work.
    Sparkel& add_clean_element(const wchar_t* str) {
        this->os << str;
        return *this;
    }

};

#endif