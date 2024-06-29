import java.util.List;

public interface BoardService {
    public BoardDTO findByBno(Integer bno);
    public List<BoardDTO> findAll();
    public Integer register(BoardDTO boardDTO);
    public void modify(BoardDTO boardDTO);
    public void remove(Integer bno);
}
