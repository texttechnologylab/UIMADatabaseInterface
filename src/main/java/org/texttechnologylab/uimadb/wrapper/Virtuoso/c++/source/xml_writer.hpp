#ifndef XML_WRITER_HPP
# define XML_WRITER_HPP

# define HEADER L"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
# define INDENT L"  "
# define NEWLINE L""

# include <string>
# include <stack>
# include <iostream>

class Writer
{
public:

  Writer(std::wostream& os) : os(os), tag_open(false), new_line(true) { os; }
  ~Writer() {}
  
  Writer& writeHeader() {
	  os << HEADER;
	  return *this;
  }

  Writer& openElt(const wchar_t* tag) {
	  this->closeTag();
	  if (elt_stack.size() > 0)
		  os << NEWLINE;
	  this->indent();
	  this->os << L"<" << tag;
	  elt_stack.push(tag);
	  tag_open = true;
	  new_line = false;
	  return *this;
  }

  Writer& quickClose() {
	  this->os << L"/";
	  this->closeTag();
	  std::wstring elt = elt_stack.top();
	  this->elt_stack.pop();
	  if (new_line)
	  {
		  os << NEWLINE;
		  this->indent();
	  }
	  new_line = true;
	  return *this;
  }

  Writer& closeElt() {
    this->closeTag();
    std::wstring elt = elt_stack.top();
    this->elt_stack.pop();
    if (new_line)
      {
        os << NEWLINE;
        this->indent();
      }
    new_line = true;
    this->os << L"</" << elt << L">";
    return *this;
  }

  Writer& closeAll() {
    while (elt_stack.size())
      this->closeElt();
  }

  Writer& attr(const wchar_t* key, const wchar_t* val) {
    this->os << L" " << key << L"=\"";
    this->write_escape(val);
    this->os << L"\"";
    return *this;
  }

  Writer& attr(const wchar_t* key, std::wstring val) {
    return attr(key, val.c_str());
  }

  Writer& attr_close(const wchar_t* key, const wchar_t* val) {
	  this->os << L" " << key << L"=\"";
	  this->write_escape(val);
	  this->os << LR"("/)";
	  this->closeTag();
	  this->os << std::endl;
	  this->elt_stack.pop();
	  return *this;
  }

  Writer& attr_close(const wchar_t* key, std::wstring val) {
	  return attr_close(key, val.c_str());
  }




  Writer& content(const wchar_t* val) {
    this->closeTag();
    this->write_escape(val);
    return *this;
  }

private:
  std::wostream& os;
  bool tag_open;
  bool new_line;
  std::stack<std::wstring> elt_stack;

  inline void closeTag() {
    if (tag_open)
      {
		this->os << L">";
        tag_open = false;
      }
  }

  inline void indent() {
    for (int i = 0; i < elt_stack.size(); i++)
      os << (INDENT);
  }

  inline void write_escape(const wchar_t* str) {
    for (; *str; str++)
      switch (*str) {
      case L'&': os << L"&amp;"; break;
      case L'<': os << L"&lt;"; break;
      case L'>': os << L"&gt;"; break;
      case L'\'': os << L"&apos;"; break;
      case L'"': os << L"&quot;"; break;
      default: os.put(*str); break;
      }
  }
};

#endif /* !XML_WRITER_HPP */