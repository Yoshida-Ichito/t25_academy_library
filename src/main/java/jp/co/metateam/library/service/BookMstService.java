package jp.co.metateam.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;
    
    @Autowired
    public BookMstService(BookMstRepository bookMstRepository){
        this.bookMstRepository = bookMstRepository;
    }
    
    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }


        return bookMstDtoList;
    }

    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {
            // BookMstDtoからBookMstへの変換
            BookMst bookMst = new BookMst();
            bookMst.setIsbn(bookMstDto.getIsbn());
            bookMst.setTitle(bookMstDto.getTitle()); 

            // データベースへの保存
            this.bookMstRepository.save(bookMst);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public int getBookMst(String isbn){
        return this.bookMstRepository.selectByIsbn(isbn);
    }


    public boolean isBookMstExist(BookMstDto bookMstDto, Model model, ArrayList<String> titleList, ArrayList<String> isbnList,boolean errorFlag) {
        //書籍名のnullチェック
    if(bookMstDto.getTitle().isEmpty()){
        titleList.add("書籍名は必須です。");
        errorFlag = true;
    } 

    //ISBNのnullチェック
    if(bookMstDto.getIsbn().isEmpty()){
        isbnList.add("ISBNは必須です。");
        errorFlag = true;
    }
    
    //ここで書籍名とisbnにエラーが合ったら、エラー情報を画面に返す
    if(errorFlag) {
        // エラーメッセージを設定
        model.addAttribute("titleErrors", titleList);
        model.addAttribute("isbnErrors", isbnList);
        // 入力値を保持
        model.addAttribute("bookMstDto", bookMstDto);
        return true;
    }

    //書籍名の桁数が256文字以上はNGのチェック
    if(bookMstDto.getTitle().length() > 256){
        titleList.add("書籍名は256文字以内で入力してください。");
        errorFlag = true;
    }

    //ISBNの桁数が13文字以外かつ半角数字以外はNGのチェック
    if(bookMstDto.getIsbn().length() != 13 || !bookMstDto.getIsbn().matches("^[0-9]+$")){
        isbnList.add("ISBNは半角数字13桁で入力してください。");
        errorFlag = true;
    }

    //ここで書籍名とisbnにエラーが合ったら、エラー情報を画面に返す
    if(errorFlag) {
        // エラーメッセージを設定
        model.addAttribute("titleErrors", titleList);
        model.addAttribute("isbnErrors", isbnList);
        // 入力値を保持
        model.addAttribute("bookMstDto", bookMstDto);
        return true;
    }

    //isbnをキーにbookMstテーブルから書籍情報を取得
    int bookMstCount = this.bookMstRepository.selectByIsbn(bookMstDto.getIsbn());

    //取得したテーブルを元に、書籍情報が存在するか確認
    if(bookMstCount > 0) {
        isbnList.add("このISBNは既に登録されています。");
        errorFlag = true;
    }

    //isbnに重複があったら、エラー情報を画面に返す
    if(errorFlag) {
        // エラーメッセージを設定
        model.addAttribute("isbnErrors", isbnList);
        // 入力値を保持
        model.addAttribute("bookMstDto", bookMstDto);
        return true;
    }  
    //エラーがなければ、falseを返す
    return false;
    }
}



