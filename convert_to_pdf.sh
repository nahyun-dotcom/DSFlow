#!/bin/bash

# DSFlow 프로젝트 문서 PDF 변환 스크립트
# 필요: pandoc, texlive-xetex, 한글 폰트

echo "🚀 DSFlow 프로젝트 문서 PDF 변환을 시작합니다..."

# 폰트 설정
KOREAN_FONT="Noto Sans CJK KR"
MONO_FONT="D2Coding"

# 출력 디렉토리 생성
mkdir -p pdf_output

echo "📊 DSFlow 프로젝트 가이드 변환 중..."
pandoc "DSFlow_프로젝트_가이드.md" \
  -o "pdf_output/DSFlow_프로젝트_가이드.pdf" \
  --pdf-engine=xelatex \
  -V mainfont="$KOREAN_FONT" \
  -V sansfont="$KOREAN_FONT" \
  -V monofont="$MONO_FONT" \
  -V geometry:margin=2cm \
  -V fontsize=11pt \
  -V linestretch=1.2 \
  --toc \
  --toc-depth=3 \
  --highlight-style=github \
  --metadata title="DSFlow 프로젝트 완전 가이드" \
  --metadata author="개발팀" \
  --metadata date="$(date '+%Y년 %m월')"

if [ $? -eq 0 ]; then
    echo "✅ DSFlow 프로젝트 가이드 PDF 변환 완료!"
else
    echo "❌ DSFlow 프로젝트 가이드 PDF 변환 실패"
fi

echo "🎯 신입 개발자 과제 변환 중..."
pandoc "신입개발자_과제.md" \
  -o "pdf_output/신입개발자_과제.pdf" \
  --pdf-engine=xelatex \
  -V mainfont="$KOREAN_FONT" \
  -V sansfont="$KOREAN_FONT" \
  -V monofont="$MONO_FONT" \
  -V geometry:margin=2cm \
  -V fontsize=11pt \
  -V linestretch=1.2 \
  --toc \
  --toc-depth=3 \
  --highlight-style=github \
  --metadata title="DSFlow 신입 개발자 온보딩 과제" \
  --metadata author="개발팀" \
  --metadata date="$(date '+%Y년 %m월')"

if [ $? -eq 0 ]; then
    echo "✅ 신입 개발자 과제 PDF 변환 완료!"
else
    echo "❌ 신입 개발자 과제 PDF 변환 실패"
fi

echo "📚 문서화 가이드 변환 중..."
pandoc "README_문서화.md" \
  -o "pdf_output/문서화_가이드.pdf" \
  --pdf-engine=xelatex \
  -V mainfont="$KOREAN_FONT" \
  -V sansfont="$KOREAN_FONT" \
  -V monofont="$MONO_FONT" \
  -V geometry:margin=2cm \
  -V fontsize=11pt \
  -V linestretch=1.2 \
  --toc \
  --toc-depth=2 \
  --highlight-style=github \
  --metadata title="DSFlow 문서화 가이드" \
  --metadata author="개발팀" \
  --metadata date="$(date '+%Y년 %m월')"

if [ $? -eq 0 ]; then
    echo "✅ 문서화 가이드 PDF 변환 완료!"
else
    echo "❌ 문서화 가이드 PDF 변환 실패"
fi

echo ""
echo "🎉 모든 PDF 변환이 완료되었습니다!"
echo "📁 생성된 파일들:"
ls -la pdf_output/

echo ""
echo "💡 사용법:"
echo "1. 각 PDF 파일을 열어서 내용을 확인하세요"
echo "2. 신입 개발자에게 전달하기 전에 내용을 검토하세요"
echo "3. 필요시 추가 스타일링이나 내용 수정 후 재변환하세요"

echo ""
echo "📞 문의사항이 있으시면 개발팀에 연락주세요!" 