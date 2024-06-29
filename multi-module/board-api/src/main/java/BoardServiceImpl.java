import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService{

    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;

    @Override
    public BoardDTO findByBno(Integer bno) {
        Optional<Board> optionalBoard = boardRepository.findById(bno);
        BoardDTO boardDTO = modelMapper.map(optionalBoard, BoardDTO.class);
        return boardDTO;
    }

    @Override
    public List<BoardDTO> findAll() { //board -> BoardDTO.class 끝까지 반복
        List<Board> lst = boardRepository.findAll();
        List<BoardDTO> boardList = lst.stream().map(board
                        -> modelMapper.map(board, BoardDTO.class))
                .collect(Collectors.toList());
        return boardList;
    }

    @Override
    public Integer register(BoardDTO boardDTO) {
        Board board = modelMapper.map(boardDTO, Board.class);
        Integer bno = boardRepository.save(board).getBno();
        return bno;
    }

    @Override
    public void modify(BoardDTO boardDTO) {
        Optional<Board> optionalBoard = boardRepository.findById(boardDTO.getBno());
        Board board = optionalBoard.orElseThrow();
        board.change(board.getTitle(), board.getContent());
        boardRepository.save(board);
    }

    @Override
    public void remove(Integer bno) {
        boardRepository.deleteById(bno);
    }
}
